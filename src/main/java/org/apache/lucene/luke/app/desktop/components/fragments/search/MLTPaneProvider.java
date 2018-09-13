package org.apache.lucene.luke.app.desktop.components.fragments.search;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.luke.app.desktop.components.TabbedPaneProvider;
import org.apache.lucene.luke.app.desktop.components.TableColumnInfo;
import org.apache.lucene.luke.app.desktop.components.util.FontUtil;
import org.apache.lucene.luke.app.desktop.components.util.TableUtil;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

public class MLTPaneProvider implements Provider<JScrollPane> {

  private final JTextField maxDocFreqTF = new JTextField();

  private final JTextField minDocFreqTF = new JTextField();

  private final JTextField minTermFreqTF = new JTextField();

  private final JTable fieldTable = new JTable();

  private final TabbedPaneProvider.TabSwitcherProxy tabSwitcher;

  private Analyzer analyzer = new StandardAnalyzer();

  @Inject
  public MLTPaneProvider(TabbedPaneProvider.TabSwitcherProxy tabSwitcher) {
    this.tabSwitcher = tabSwitcher;
  }

  public void setAnalyzer(Analyzer analyzer) {
    this.analyzer = analyzer;
  }

  @Override
  public JScrollPane get() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
    panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

    panel.add(mltParams());
    panel.add(new JSeparator(JSeparator.HORIZONTAL));
    panel.add(analyzerNamePane());
    panel.add(new JSeparator(JSeparator.HORIZONTAL));
    panel.add(fieldsSettings());

    return new JScrollPane(panel);
  }

  private JPanel mltParams() {
    JPanel panel = new JPanel(new GridLayout(3, 1));

    JPanel maxDocFreq = new JPanel(new FlowLayout(FlowLayout.LEADING));
    maxDocFreq.add(new JLabel(MessageUtils.getLocalizedMessage("search_mlt.label.max_doc_freq")));
    maxDocFreqTF.setColumns(10);
    maxDocFreq.add(maxDocFreqTF);
    panel.add(maxDocFreq);

    JPanel minDocFreq = new JPanel(new FlowLayout(FlowLayout.LEADING));
    minDocFreq.add(new JLabel(MessageUtils.getLocalizedMessage("search_mlt.label.min_doc_freq")));
    minDocFreqTF.setColumns(5);
    minDocFreq.add(minDocFreqTF);
    panel.add(minDocFreq);

    JPanel minTermFreq = new JPanel(new FlowLayout(FlowLayout.LEADING));
    minTermFreq.add(new JLabel(MessageUtils.getLocalizedMessage("serach_mlt.label.min_term_freq")));
    minTermFreqTF.setColumns(5);
    minTermFreq.add(minTermFreqTF);
    panel.add(minTermFreq);

    return panel;
  }

  private JPanel analyzerNamePane() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));

    panel.add(new JLabel(MessageUtils.getLocalizedMessage("search_mlt.label.analyzer")));

    JLabel analyzerLbl = new JLabel(analyzer.getClass().getName());
    panel.add(analyzerLbl);

    JLabel changeLbl = new JLabel(MessageUtils.getLocalizedMessage("search_mlt.hyperlink.change"));
    changeLbl.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        tabSwitcher.switchTab(TabbedPaneProvider.Tab.ANALYZER);
      }
    });
    panel.add(FontUtil.toLinkText(changeLbl));

    return panel;
  }

  private JPanel fieldsSettings() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setPreferredSize(new Dimension(500, 300));
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    JPanel header = new JPanel(new GridLayout(2, 1));
    header.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
    header.add(new JLabel(MessageUtils.getLocalizedMessage("search_mlt.label.description")));
    JCheckBox loadAllCB = new JCheckBox(MessageUtils.getLocalizedMessage("search_mlt.checkbox.select_all"));
    header.add(loadAllCB);
    panel.add(header, BorderLayout.PAGE_START);

    TableUtil.setupTable(fieldTable, ListSelectionModel.SINGLE_SELECTION, new MLTFieldTableModel(), null, 50);
    panel.add(new JScrollPane(fieldTable), BorderLayout.CENTER);

    return panel;
  }

}

class MLTFieldTableModel extends AbstractTableModel {

  enum Column implements TableColumnInfo {
    SELECT("Select", 0, Boolean.class),
    String("Field", 1, String.class);

    private String colName;
    private int index;
    private Class<?> type;

    Column(String colName, int index, Class<?> type) {
      this.colName = colName;
      this.index = index;
      this.type = type;
    }

    @Override
    public String getColName() {
      return colName;
    }

    @Override
    public int getIndex() {
      return index;
    }

    @Override
    public Class<?> getType() {
      return type;
    }
  }

  private static final Map<Integer, Column> columnMap = TableUtil.columnMap(Column.values());

  private final String[] colNames = TableUtil.columnNames(Column.values());

  private final Object[][] data;

  MLTFieldTableModel() {
    this.data = new Object[0][colNames.length];
  }

  @Override
  public int getRowCount() {
    return data.length;
  }

  @Override
  public int getColumnCount() {
    return colNames.length;
  }

  @Override
  public String getColumnName(int colIndex) {
    if (columnMap.containsKey(colIndex)) {
      return columnMap.get(colIndex).colName;
    }
    return "";
  }

  @Override
  public Class<?> getColumnClass(int colIndex) {
    if (columnMap.containsKey(colIndex)) {
      return columnMap.get(colIndex).type;
    }
    return Object.class;
  }


  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    return data[rowIndex][columnIndex];
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    if (columnIndex == Column.SELECT.getIndex()) {
      return true;
    }
    return false;
  }

  @Override
  public void setValueAt(Object value, int rowIndex, int columnIndex) {
    data[rowIndex][columnIndex] = value;
    fireTableCellUpdated(rowIndex, columnIndex);
  }

}
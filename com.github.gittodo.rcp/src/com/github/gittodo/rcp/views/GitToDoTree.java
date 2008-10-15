package com.github.gittodo.rcp.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.openscience.gittodo.model.IGTDRepository;
import org.openscience.gittodo.model.Item;
import org.openscience.gittodo.model.Repository;
import org.openscience.gittodo.sort.ItemSorter;

public class GitToDoTree extends TableViewer {

    private final GitToDoTree gtdTree;
    private final GitToDoTreeFilter treeFilter;
    private final Table table;
    private Shell shell;
    private List<Item> items;
    private List<Item> activeItems;
    
    public GitToDoTree(Shell parent) {
        super(parent, SWT.SINGLE | SWT.FULL_SELECTION | SWT.FILL);
        this.treeFilter = new GitToDoTreeFilter();
        this.treeFilter.reset();
        this.shell = parent;
        this.gtdTree = this;
        table = this.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        IGTDRepository repos = new Repository();
        items = new ArrayList<Item>();
        items.addAll(repos.items().values());

        String[] columnNames = new String[] {
            "Created", "Project", "Context", "Priority", "Title"
        };
        int[] columnWidths = new int[] {
            120, 150, 100, 100, 300
        };
        int[] columnAlignments = new int[] {
            SWT.RIGHT, SWT.LEFT, SWT.LEFT, SWT.LEFT, SWT.LEFT
        };
        for (int i=0; i<columnNames.length; i++) {
            TableColumn column = new TableColumn(table, columnAlignments[i]);
            column.setText(columnNames[i]);
            column.setWidth(columnWidths[i]);
            column.addSelectionListener(new ColumnSorter(i, items));
        }
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                super.mouseDoubleClick(e);
                int index = table.getSelectionIndex();
                try {
                    Item selected = (Item)table.getItem(index).getData();
                    ItemEditShell child = new ItemEditShell(gtdTree.shell, selected, gtdTree);
                    child.open();
                } catch ( Exception exception) {
                    exception.printStackTrace();
                }
            }
        });
        
        this.setLabelProvider(new ItemTableLabelProvider());
        this.setContentProvider(new ArrayContentProvider());
        setActiveItems(items);
    }

    public List<Item> getItems() {
        return items;
    }
    
    public List<Item> getActiveItems() {
        return items;
    }
    
    public void setActiveItems(List<Item> items) {
        activeItems = new ArrayList<Item>();
        for (Item item : items) if (treeFilter.matches(item)) activeItems.add(item);
        Item[] itemArray = (Item[])activeItems.toArray(new Item[]{}); 
        this.setInput(itemArray);
    }
    
    class ColumnSorter implements SelectionListener {
        private int colIndex;
        private List<Item> items;
        
        private ColumnSorter(int index, List<Item> items) {
            this.colIndex = index;
            this.items = items;
        }
        
        public void widgetDefaultSelected( SelectionEvent arg0 ) {
//            System.out.println("Column default: " + arg0.getClass().getName());
        }

        public void widgetSelected( SelectionEvent arg0 ) {
            System.out.println("Column selected: " + arg0.getClass().getName());
            switch (colIndex) {
                case 0:
                    setActiveItems(ItemSorter.sortByCreationDate(items));
                    break;
                case 1:
                    setActiveItems(ItemSorter.sortByProject(items));
                    break;
                case 2:
                    setActiveItems(ItemSorter.sortByContext(items));
                    break;
                case 3:
                    setActiveItems(ItemSorter.sortByPriority(items));
                    break;
                case 4:
                    setActiveItems(ItemSorter.sortByTitle(items));
                    break;
                default:
                    return;            
            }
        }
    }
    
    public void update() {
        setActiveItems(getItems());
        table.update();
    }

    public GitToDoTreeFilter getFilter() {
        return treeFilter;
    }
}
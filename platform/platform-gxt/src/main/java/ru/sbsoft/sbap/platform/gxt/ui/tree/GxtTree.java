package ru.sbsoft.sbap.platform.gxt.ui.tree;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.widget.core.client.Window;
import java.util.List;
import sbap.definitions.application.TDatasource_I;
import sbap.definitions.application.TTreeItem_I;
import com.sencha.gxt.widget.core.client.tree.Tree;
import ru.sbsoft.sbap.platform.dflt.utils.ListeningList;
import ru.sbsoft.sbap.platform.gxt.context.ContextConverter;
import ru.sbsoft.sbap.schema.ui.AbstractRegister;
import ru.sbsoft.sbap.schema.ui.ApplicationContext;
import sbap.definitions.application.TCustomScript_I;
import sbap.definitions.application.TForm_I;
import sbap.definitions.application.TShowFormAction_I;
import sbap.definitions.application.TStartOperationAction_I;
import sbap.definitions.application.Tree_I;

/**
 *
 * @author Fedor Resnyanskiy, SBSOFT
 */
public class GxtTree implements Tree_I, IsWidget {

    private Tree<TTreeItem_I, String> tree;
    private TreeStore<TTreeItem_I> store = new TreeStore<>(new TreeItemKeyProvider());
    private ListeningList<TTreeItem_I> itemList = new ListeningList<>();

    private GxtDatasource ds;
    private final ApplicationContext applicationContext;

    public GxtTree(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        itemList.addAddListener(i -> addListItem(i));

        tree = new Tree<>(store, new TreeItemValueProvider());
        tree.addStyleName("sbap-main-menu");
        tree.getSelectionModel().addSelectionHandler(
                e -> Scheduler.get().scheduleFinally(
                        () -> executeMenu(e.getSelectedItem())
                )
        );
    }

    private void executeMenu(TTreeItem_I item) {
        final TTreeItem_I.SelectActions_I actions = item.getSelectActions();
        actions.getShowForm().forEach(a -> {
            final String formName = ((TShowFormAction_I) a).getFormName();
            final AbstractRegister<TForm_I> forms = applicationContext.getRegisters().forms();
            windowize((IsWidget) forms.get(formName, applicationContext)).show();
        });
        actions.getStartOperation().forEach(a -> {
            alert("Start operation " + ((TStartOperationAction_I) a).getOperationName());
        });
        actions.getCustom().forEach(a -> {
            execute(createContext(applicationContext), ((TCustomScript_I) a).getScript());
        });
    }

    private Window windowize(IsWidget w) {
        final Window window = new Window();
        window.setWidth(300);
        window.setHeight(200);
        window.add(w);
        return window;
    }

    private JavaScriptObject createContext(ApplicationContext applicationContext) {
        return ContextConverter.getInstance().convert(applicationContext);
    }

    private native void execute(JavaScriptObject appCtx, String script)/*-{
        var a = appCtx + script;
        eval('var appContext = arguments[0];');
        eval(script);
    }-*/;

    private native void alert(String message)/*-{
        alert(message);
    }-*/;

    @Override
    public GxtDatasource getDatasource() {
        return ds;
    }

    @Override
    public void setDatasource(TDatasource_I value) {
        this.ds = (GxtDatasource) value;
    }

    @Override
    public List<TTreeItem_I> getItemList() {
        return itemList;
    }

    @Override
    public void setItemList(List<TTreeItem_I> itemList) {
        this.itemList.clear();
        this.itemList.addAll(itemList);
    }

    @Override
    public Widget asWidget() {
        return tree;
    }

    private void addListItem(TTreeItem_I i) {
        store.add(i);
        i.getChildList().forEach(c -> addListSubItem(i, c));
        if (i.getChildList() instanceof ListeningList) {
            ((ListeningList<TTreeItem_I>) i.getChildList()).addAddListener(c -> addListSubItem(i, c));
        }
    }

    private void addListSubItem(TTreeItem_I parent, TTreeItem_I item) {
        store.add(parent, item);
    }

    private static class TreeItemKeyProvider implements ModelKeyProvider<TTreeItem_I> {

        @Override
        public String getKey(TTreeItem_I item) {
            return item.getCaption().getValue();
        }
    }

    private static class TreeItemValueProvider implements ValueProvider<TTreeItem_I, String> {

        @Override
        public String getValue(TTreeItem_I object) {
            return object.getCaption().getValue();
        }

        @Override
        public void setValue(TTreeItem_I object, String value) {

        }

        @Override
        public String getPath() {
            return "captionValue";
        }

    }

}

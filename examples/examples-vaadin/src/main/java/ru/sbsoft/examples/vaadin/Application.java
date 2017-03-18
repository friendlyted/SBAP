package ru.sbsoft.examples.vaadin;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.data.Item;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Tree;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import java.util.Arrays;
import java.util.Date;
import ru.sbsoft.examples.application.forms.Form2Factory;
import ru.sbsoft.examples.application.forms.SingleTabFormFactory;
import ru.sbsoft.examples.application.model.AllTypesModel;
import ru.sbsoft.sbf.app.form.IHasName;
import ru.sbsoft.sbf.app.form.builder.FormBuilder;
import ru.sbsoft.sbf.app.model.YearMonthDay;
import ru.sbsoft.sbf.vaadin.app.FormElementVaadinFactory;

/**
 *
 * @author Fedor Resnyanskiy
 */
@Title("My UI")
@Theme("debugTheme")
public class Application extends UI {

    public static final String FORMS = "Forms";
    public static final String TEST_TAB = "Test tab form";
    public static final String SINGLE_TAB = "Single tab form";
    public static final String FIELDSET = "Fieldset Title&Border";

    private final static AllTypesModel MODEL = new AllTypesModel();

    static {
        MODEL.setBooleanValue(Boolean.TRUE);
        MODEL.setDateValue(new Date());
        MODEL.setIntegerValue(Integer.MAX_VALUE);
        MODEL.getListValue().addAll(Arrays.asList(MODEL, MODEL, MODEL));
        MODEL.setLongValue(Long.MAX_VALUE);
        MODEL.setStringValue("stringValue");
        MODEL.setYmdDate(new YearMonthDay(2000, 1, 1));
    }

    @Override
    protected void init(VaadinRequest request) {
        // Create the content root layout for the UI
        HorizontalLayout content = new HorizontalLayout();
        setContent(content);

        final Tree tree = new Tree("formsTree");
        // Display the greeting
        content.addComponent(tree);

        final Item formsItem = tree.addItem(FORMS);

        tree.addExpandListener(new Tree.ExpandListener() {
            @Override
            public void nodeExpand(Tree.ExpandEvent event) {
                if (FORMS.equals(event.getItemId().toString())) {
                    //final Item testTabItem = tree.addItem(TEST_TAB);
                    final Item singleTabItem = tree.addItem(SINGLE_TAB);
                    final Item fieldsetItem = tree.addItem(FIELDSET);

                    //tree.setParent(TEST_TAB, FORMS);
                    tree.setParent(SINGLE_TAB, FORMS);
                    tree.setParent(FIELDSET, FORMS);
                }
            }
        });

        tree.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent event) {
                final String selected = event.getItemId().toString();
                if (TEST_TAB.equals(selected)) {
                    showForm(
                            new FormBuilder(FormElementVaadinFactory.getInstance()).setName("test")
                            .addTab().name("tab1")
                            .addFieldset().name("fieldset1").tab()
                            .addFieldset().name("fieldset2").text("text")
                            .form()
                    );
                } else if (SINGLE_TAB.equals(selected)) {
                    showForm(SingleTabFormFactory.createForm(FormElementVaadinFactory.getInstance()).setName(SINGLE_TAB));
                } else if (FIELDSET.equals(selected)) {
                    showForm(Form2Factory.createForm(FormElementVaadinFactory.getInstance()).setName(FIELDSET));
                }
            }
        });
    }

    private void showForm(FormBuilder formBuilder) {
        formBuilder.getController().writeFrom(MODEL);
        
        final Window window = new Window("Wintest Application");
        window.setContent((Component) formBuilder.getElement());
        window.setCaption(((IHasName) formBuilder.getElement()).getName());
        window.setWidth(800, Unit.PIXELS);
        window.setHeight(600, Unit.PIXELS);
        addWindow(window);
    }
}

package ru.sbsoft.sbap.platform.gxt.ui.tree;

import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.TreeStore;

public class GxtTestStore {

    public static TreeStore<String> getTestStore(){
        final TreeStore<String> store = new TreeStore<>(new KeyProvider());
        
        store.add("item1");
        store.add("item2");
        store.add("item3");
        
        store.add("item3","item4");
        store.add("item3","item5");
        store.add("item3","item6");
        
        
        
        return store;
    }
    
    public static class KeyProvider implements ModelKeyProvider<String> {

        @Override
        public String getKey(String item) {
            return item;
        }
    }

    public static class ValueProvider implements com.sencha.gxt.core.client.ValueProvider<String, String> {

        @Override
        public String getValue(String object) {
            return object;
        }

        @Override
        public void setValue(String object, String value) {

        }

        @Override
        public String getPath() {
            return null;
        }
    }
    
    
}

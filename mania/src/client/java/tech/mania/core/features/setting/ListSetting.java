package tech.mania.core.features.setting;


import tech.mania.core.types.setting.Setting;
import tech.mania.core.types.setting.Visibility;

public class ListSetting extends Setting {

    private final Setting[] settings;

    private ListSetting(String name, Visibility visibility, Setting[] settings) {
        super(name, visibility);
        this.settings = settings;
    }

    public Setting[] getSettings() {
        return settings;
    }

    public static ListBuilder build() {
        return new ListBuilder();
    }

    public static class ListBuilder extends Builder<ListSetting, ListBuilder> {

        private Setting[] settings;

        private ListBuilder() {
            super();
            this.settings = new Setting[0];
        }

        public ListBuilder settings(Setting... settings) {
            this.settings = settings;
            return this;
        }

        @Override
        public ListSetting end() {
            return new ListSetting(
                    this.name,
                    this.visibility,
                    this.settings
            );
        }
    }
}

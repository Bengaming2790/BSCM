package ca.techgarage.bscm.client;

import ca.techgarage.bscm.Bscm;
import ca.techgarage.bscm.Comment;
import ca.techgarage.bscm.ConfigManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class BSCMConfigScreen extends Screen {

    private final Screen parent;
    private final Class<?> configClass;
    private final String filename;

    private record FieldRow(Field field, Object widget) {}
    private final List<FieldRow> rows = new ArrayList<>();

    private static final int ROW_HEIGHT = 24;
    private static final int LABEL_WIDTH = 220;
    private static final int WIDGET_WIDTH = 150;
    private static final int START_Y = 40;

    public BSCMConfigScreen(Screen parent, Class<?> configClass, String filename) {
        super(Text.literal("Config — " + filename));
        this.parent = parent;
        this.configClass = configClass;
        this.filename = filename;
    }

    @Override
    protected void init() {
        rows.clear();

        List<Field> fields = ConfigManager.declaredStaticFields(configClass);
        int y = START_Y;

        for (Field field : fields) {
            field.setAccessible(true);
            Class<?> type = field.getType();
            int x = this.width / 2 - WIDGET_WIDTH / 2 + LABEL_WIDTH / 2;

            try {
                if (type == boolean.class) {
                    boolean current = field.getBoolean(null);
                    CyclingButtonWidget<Boolean> btn = CyclingButtonWidget.onOffBuilder(current)
                            .build(x, y, WIDGET_WIDTH, 20,
                                    Text.literal(getLabel(field)),
                                    (button, value) -> {});
                    addDrawableChild(btn);
                    rows.add(new FieldRow(field, btn));

                } else if (type == int.class || type == double.class
                        || type == float.class || type == long.class
                        || type == String.class) {
                    TextFieldWidget text = new TextFieldWidget(
                            this.textRenderer, x, y, WIDGET_WIDTH, 20,
                            Text.literal(getLabel(field)));
                    text.setText(String.valueOf(field.get(null)));
                    text.setMaxLength(64);
                    addDrawableChild(text);
                    rows.add(new FieldRow(field, text));
                }

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            y += ROW_HEIGHT + 4;
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), btn -> saveAndClose())
                .dimensions(this.width / 2 - 100, this.height - 30, 200, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(
                textRenderer, this.title, this.width / 2, 12, 0xFFFFFF);

        List<Field> fields = ConfigManager.declaredStaticFields(configClass);
        int y = START_Y;
        for (Field field : fields) {
            int labelX = this.width / 2 - WIDGET_WIDTH / 2 + LABEL_WIDTH / 2 - LABEL_WIDTH - 4;
            context.drawTextWithShadow(
                    textRenderer, getLabel(field), labelX, y + 5, 0xA0A0A0);
            y += ROW_HEIGHT + 4;
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        saveAndClose();
    }

    private void saveAndClose() {
        for (FieldRow row : rows) {
            try {
                Field field = row.field();
                Class<?> type = field.getType();

                if (row.widget() instanceof CyclingButtonWidget<?> btn) {
                    field.setBoolean(null, (Boolean) btn.getValue());
                } else if (row.widget() instanceof TextFieldWidget text) {
                    String raw = text.getText().trim();
                    if      (type == int.class)    field.setInt   (null, Integer.parseInt(raw));
                    else if (type == double.class)  field.setDouble(null, Double.parseDouble(raw));
                    else if (type == float.class)   field.setFloat (null, Float.parseFloat(raw));
                    else if (type == long.class)    field.setLong  (null, Long.parseLong(raw));
                    else if (type == String.class)  field.set      (null, raw);
                }
            } catch (NumberFormatException | IllegalAccessException e) {
                System.err.println("[BSCM] Invalid input for " + row.field().getName());
            }
        }

        Bscm.save(configClass, filename);
        this.client.setScreen(parent);
    }

    private static String getLabel(Field field) {
        Comment comment = field.getAnnotation(Comment.class);
        return comment != null ? comment.value() : field.getName();
    }
}
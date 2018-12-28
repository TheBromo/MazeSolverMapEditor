package ch.bbw.model.fields;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;

public abstract class Field {
    private int x, y;

    public static void writeString(String val, ByteBuffer buffer) {
        byte[] data = val.getBytes();
        buffer.putInt(data.length);
        buffer.put(data);
    }

    public static String readString(ByteBuffer buffer) {
        byte[] data = new byte[buffer.getInt()];
        buffer.get(data);
        return new String(data);
    }

    public static Field createField(String className) {
        try {
            Class<Field> packetClass = Class.forName(className).asSubclass((Class) Field.class);
            return packetClass.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void compileField(Field field, ByteBuffer byteBuffer) {
        writeString(field.getClass().getName(), byteBuffer);
        byteBuffer.putInt(field.x);
        byteBuffer.putInt(field.y);
    }

    public static Field decompileField(ByteBuffer byteBuffer) {
        Field field = createField(readString(byteBuffer));
        field.x = byteBuffer.getInt();
        field.y = byteBuffer.getInt();
        return field;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}

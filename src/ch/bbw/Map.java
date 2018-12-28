package ch.bbw;

import ch.bbw.model.fields.Field;

import java.awt.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

class Map {
    void exportMap(Field[][] fields) throws IOException {


        ByteBuffer buffer = ByteBuffer.allocate((40) * fields.length * fields[0].length + 8);
        buffer.putInt(fields.length);
        buffer.putInt(fields[0].length);
        for (Field[] array : fields) {
            for (Field field : array) {
                Field.compileField(field, buffer);
            }
        }
        FileDialog dialog = new FileDialog((Frame) null, "Export file");
        dialog.setMode(FileDialog.SAVE);
        dialog.setFile("file.maze");
        dialog.setVisible(true);
        String filename = dialog.getFile();
        if (filename == null) return;
        if (!filename.contains(".maze")) filename += ".maze";
        writeFile(buffer.array(), filename);
    }

    Field[][] importMap() throws IOException {
        FileDialog dialog = new FileDialog((Frame) null, "Select file to import");
        dialog.setMode(FileDialog.LOAD);
        dialog.setFile("*.maze");
        dialog.setVisible(true);
        String filename = dialog.getFile();
        if (filename == null) return null;
        ByteBuffer buffer = ByteBuffer.wrap(getFileText(filename));
        int w = buffer.getInt();
        int h = buffer.getInt();
        Field[][] fields = new Field[w][h];
        for (int x = 0; x < fields.length; x++) {
            for (int y = 0; y < fields[x].length; y++) {
                fields[x][y] = Field.decompileField(buffer);
            }
        }
        return fields;
    }

    private byte[] getFileText(String path) throws IOException {
        return Base64.getDecoder().decode(Files.readAllBytes(Paths.get(path)));
    }

    private void writeFile(byte[] data, String dir) throws IOException {
        Files.write(Paths.get(dir), Base64.getEncoder().encode(data));
    }
}

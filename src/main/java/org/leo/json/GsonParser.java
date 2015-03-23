package org.leo.json;

import java.io.IOException;
import java.io.Reader;
import java.util.Stack;

import org.json.simple.parser.ContentHandler;
import org.json.simple.parser.ParseException;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

public class GsonParser {

    private enum EntryType {
        OBJECT,
        ARRAY,
        PRIMITIVE
    }

    public void parse(Reader reader, ContentHandler contentHandler) throws IOException, ParseException {
        JsonReader jsonReader = new JsonReader(reader);
        Stack<EntryType> entryStack = new Stack<>();
        while (true) {
            JsonToken token = jsonReader.peek();
            switch (token) {
                case BEGIN_ARRAY:
                    jsonReader.beginArray();
                    if (!contentHandler.startArray()) {
                        return;
                    }
                    break;
                case END_ARRAY:
                    jsonReader.endArray();
                    if (!contentHandler.endArray()) {
                        return;
                    }
                    if (entryStack.peek() == EntryType.ARRAY) {
                        if (!contentHandler.endObjectEntry()) {
                            return;
                        }
                        entryStack.pop();
                    }
                    break;
                case BEGIN_OBJECT:
                    jsonReader.beginObject();
                    if (!contentHandler.startObject()) {
                        return;
                    }
                    break;
                case END_OBJECT:
                    jsonReader.endObject();
                    if (!contentHandler.endObject()) {
                        return;
                    }
                    if (entryStack.peek() == EntryType.OBJECT) {
                        if (!contentHandler.endObjectEntry()) {
                            return;
                        }
                        entryStack.pop();
                    }
                    break;
                case NAME:
                    String name = jsonReader.nextName();
                    if (!contentHandler.startObjectEntry(name)) {
                        return;
                    }
                    JsonToken peek = jsonReader.peek();
                    if (peek == JsonToken.STRING || peek == JsonToken.BOOLEAN || peek == JsonToken.NUMBER
                        || peek == JsonToken.NULL) {
                        entryStack.push(EntryType.PRIMITIVE);
                    } else if (peek == JsonToken.BEGIN_OBJECT) {
                        entryStack.push(EntryType.OBJECT);
                    } else if (peek == JsonToken.BEGIN_ARRAY) {
                        entryStack.push(EntryType.ARRAY);
                    }
                    break;
                case STRING:
                    String s = jsonReader.nextString();
                    if (!contentHandler.primitive(s)) {
                        return;
                    }
                    if (entryStack.peek() == EntryType.OBJECT) {
                        if (!contentHandler.endObjectEntry()) {
                            return;
                        }
                        entryStack.pop();
                    }
                    break;
                case NUMBER:
                    String n = jsonReader.nextString();
                    if (!contentHandler.primitive(Double.parseDouble(n))) {
                        return;
                    }
                    if (entryStack.peek() == EntryType.OBJECT) {
                        if (!contentHandler.endObjectEntry()) {
                            return;
                        }
                        entryStack.pop();
                    }
                    break;
                case BOOLEAN:
                    boolean b = jsonReader.nextBoolean();
                    if (!contentHandler.primitive(b)) {
                        return;
                    }
                    if (entryStack.peek() == EntryType.OBJECT) {
                        if (!contentHandler.endObjectEntry()) {
                            return;
                        }
                        entryStack.pop();
                    }
                    break;
                case NULL:
                    jsonReader.nextNull();
                    if (!contentHandler.primitive(null)) {
                        return;
                    }
                    if (entryStack.peek() == EntryType.OBJECT) {
                        if (!contentHandler.endObjectEntry()) {
                            return;
                        }
                        entryStack.pop();
                    }
                    break;
                case END_DOCUMENT:
                    entryStack.clear();
                    return;
            }
        }
    }

}

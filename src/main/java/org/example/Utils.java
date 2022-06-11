package org.example;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class Utils {


    public static int getParameterCount(String descriptor){
        //Count the ammount of Semicolons in the method descriptor to get the number of parameters
       return (int) Arrays.stream(ArrayUtils.toObject(descriptor.toCharArray())).filter(c -> c==';').count();
    }

    public static String getOpcodeNameFromValue(int value) {
        Optional<Field> foundField = Arrays.stream(Opcodes.class.getFields()).filter(field -> {
            try {
                if (field.getType() == Integer.class) {
                    return Objects.equals((Integer) field.get(null), value);
                }else{
                    return field.getInt(null)==value;
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }).findFirst();

        return foundField.map(Field::getName).orElse("");

    }


}

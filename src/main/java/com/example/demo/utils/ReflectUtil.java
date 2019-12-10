package com.example.demo.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class ReflectUtil {

    public static <T> int getFieldModifier(Class<T> clazz, String field) throws Exception {
        Field[] fields = getFields(clazz);

        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getName().equals(field)) {
                return fields[i].getModifiers();
            }
        }
        throw new Exception(clazz + " has no field \"" + field + "\"");
    }

    public static <T> int getMethodModifier(Class<T> clazz, String method) throws Exception {

        Method[] m = clazz.getMethods(); // clazz.getDeclaredMethods();

        for (int i = 0; i < m.length; i++) {
            if (m[i].getName().equals(m)) {
                return m[i].getModifiers();
            }
        }
        throw new Exception(clazz + " has no method \"" + m + "\"");
    }

    public static <T> Object getFieldValue(Object clazzInstance, Object field)
            throws IllegalArgumentException, IllegalAccessException {

        Field[] fields = getFields(clazzInstance.getClass());

        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getName().equals(field)) {
                fields[i].setAccessible(true);
                return fields[i].get(clazzInstance);
            }
        }
        return null;
    }

    public static <T> Object getFieldValue(Class<T> clazz, String field)
            throws IllegalArgumentException, IllegalAccessException, InstantiationException {

        Field[] fields = getFields(clazz);

        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getName().equals(field)) {
                fields[i].setAccessible(true);
                return fields[i].get(clazz.newInstance());
            }
        }

        return null;
    }

    public static void setFieldValue(Object obj, String fieldName, String fieldValue)
            throws IllegalArgumentException, IllegalAccessException {
        Field field = getField(obj.getClass(), fieldName);
        if (field != null) {
            field.setAccessible(true);
            field.set(obj, fieldValue);
        }
    }

    public static <T> Field getField(Class<T> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            if (field == null) {
                field = clazz.getSuperclass().getDeclaredField(fieldName);
            }
            return field;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> Field[] getFields(Class<T> clazz) {

        Field[] superFields = clazz.getSuperclass().getDeclaredFields();
        Field[] extFields = clazz.getDeclaredFields();
        Field[] fields = new Field[superFields.length + extFields.length];
        System.arraycopy(superFields, 0, fields, 0, superFields.length);
        System.arraycopy(extFields, 0, fields, superFields.length, extFields.length);

        return fields;
    }

    public static <T> Method[] getMethods(Class<T> clazz) {
        Method[] superMethods = clazz.getSuperclass().getDeclaredMethods();
        Method[] extMethods = clazz.getDeclaredMethods();
        Method[] methods = new Method[superMethods.length + extMethods.length];
        System.arraycopy(superMethods, 0, methods, 0, superMethods.length);
        System.arraycopy(extMethods, 0, methods, superMethods.length, extMethods.length);

        return methods;
    }

    public static <T> Method getMethod(Class<T> clazz, String methodName) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method m : methods) {
            if (m.getName().equals(methodName)) {
                return m;
            }
        }
        methods = clazz.getSuperclass().getDeclaredMethods();
        for (Method m : methods) {
            if (m.getName().equals(methodName)) {
                return m;
            }
        }
        return null;
    }

    public static <T> Field[] getPersistFields(Class<T> clazz) {
        Field[] fields = getFields(clazz);

        List<Field> result = new ArrayList<Field>();

        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getModifiers() != Modifier.PRIVATE) {
                continue;
            }
            result.add(fields[i]);
        }

        return result.toArray(new Field[] {});
    }
}
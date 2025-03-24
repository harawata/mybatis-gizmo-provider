/*-
 * MIT License
 *
 * Copyright (c) 2025 Iwao AVE!
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.harawata.mgp;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.ibatis.binding.MapperMethod.ParamMap;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.builder.annotation.ProviderMethodResolver;

import jakarta.persistence.Column;
import jakarta.persistence.Table;

public abstract class GizmoProvider implements ProviderMethodResolver {

  private static Method insert;
  private static Method upsert;
  private static Method update;

  private static Map<Class<?>, List<Field>> fieldCache = new ConcurrentHashMap<>();

  static {
    for (Method method : GizmoProvider.class.getDeclaredMethods()) {
      String name = method.getName();
      if ("insert".equals(name)) {
        insert = method;
      } else if ("upsert".equals(name)) {
        upsert = method;
      } else if ("update".equals(name)) {
        update = method;
      }
    }
  }

  @Override
  public Method resolveMethod(ProviderContext context) {
    String mapperMethodName = context.getMapperMethod().getName();
    if (mapperMethodName.startsWith("insert")) {
      return insert;
    } else if (mapperMethodName.startsWith("upsert")) {
      return upsert;
    } else if (mapperMethodName.startsWith("update")) {
      return update;
    }
    return null;
  }

  public StringBuilder insert(Object bean, ProviderContext context) {
    Class<? extends Object> beanClass = bean.getClass();
    List<Field> fields = getInsertableFields(beanClass);
    StringBuilder sql = new StringBuilder();

    sql.append("insert into ");
    catalogOrSchema(beanClass).ifPresent(x -> sql.append(escape(x)).append('.'));
    sql.append(escape(getTableName(beanClass))).append(" (");

    StringBuilder values = new StringBuilder();
    for (int i = 0; i < fields.size(); i++) {
      if (i > 0) {
        sql.append(", ");
        values.append(", ");
      }
      sql.append(escape(getColumnName(fields.get(i))));
      values.append("#{").append(fields.get(i).getName()).append('}');
    }
    sql.append(") values (").append(values).append(')');
    return sql;
  }

  @SuppressWarnings("unchecked")
  public StringBuilder update(ProviderContext context, Object param) {
    Method mapperMethod = context.getMapperMethod();
    String mapperMethodName = mapperMethod.getName();
    int declaredParamCount = mapperMethod.getParameterCount();

    int byPos = mapperMethodName.indexOf("By");
    if (declaredParamCount == 1 && byPos == -1) {
      throw new IllegalArgumentException(
          "Unable to add conditions to UPDATE. "
              + "You can specify the bean's properties with method name (e.g. 'updateByIdAndName(Bean bean)')"
              + " or add parameters (e.g. 'update(Bean bean, Integer id, String name)').");
    }

    Parameter[] methodArgs = mapperMethod.getParameters();
    if (declaredParamCount == 0) {
      throw new IllegalArgumentException("There must be a parameter.");
    }

    Class<?> beanClass;
    String beanArgName;
    Map<String, Object> params;
    if (param instanceof ParamMap) {
      params = (Map<String, Object>) param;
      beanArgName = methodArgs[0].getName();
      beanClass = params.get(beanArgName).getClass();
    } else {
      beanArgName = "";
      beanClass = param.getClass();
      params = Collections.emptyMap();
    }

    List<Field> fields = getUpdatableFields(beanClass);
    StringBuilder sql = new StringBuilder();
    sql.append("update ");
    catalogOrSchema(beanClass).ifPresent(x -> sql.append(escape(x)).append('.'));
    sql.append(escape(getTableName(beanClass))).append(" set ");
    // SET clause
    for (int i = 0; i < fields.size(); i++) {
      if (i > 0) {
        sql.append(", ");
      }
      sql.append(escape(getColumnName(fields.get(i))))
          .append(" = #{");
      if (!beanArgName.isEmpty()) {
        sql.append(beanArgName).append('.');
      }
      sql.append(fields.get(i).getName()).append('}');
    }

    // WHERE clause
    sql.append(" where ");
    String[] criteria = mapperMethodName.split("By(?=[A-Z])|And(?=[A-Z])");
    if (criteria.length == 1) {
      // Method name does not contain condition columns
      // Use the arg names
      for (int i = 1; i < methodArgs.length; i++) {
        String methodArgName = methodArgs[i].getName();
        if (i > 1) {
          sql.append(" and ");
        }
        sql.append(escape(toColumnName(methodArgName))).append(" = #{");
        sql.append(methodArgName).append('}');
      }
    } else {
      // Method name contains condition columns
      for (int i = 1; i < criteria.length; i++) {
        String col = decapitalize(criteria[i]);
        if (i > 1) {
          sql.append(" and ");
        }
        sql.append(escape(toColumnName(col))).append(" = #{");
        if (!params.containsKey(col) && !beanArgName.isEmpty()) {
          sql.append(beanArgName).append('.');
        }
        sql.append(col).append('}');
      }
    }
    return sql;
  }

  protected List<Field> getInsertableFields(Class<? extends Object> beanClass) {
    return getFields(beanClass, f -> {
      if (ignoredTypes(f.getType())) {
        return false;
      }
      Column column = f.getAnnotation(Column.class);
      return column == null || column.insertable();
    });
  }

  protected List<Field> getUpdatableFields(Class<?> beanClass) {
    return getFields(beanClass, f -> {
      if (ignoredTypes(f.getType())) {
        return false;
      }
      Column column = f.getAnnotation(Column.class);
      return column == null || column.updatable();
    });
  }

  protected boolean ignoredTypes(Class<?> fieldType) {
    if (fieldType.isPrimitive()) {
      return false;
    }
    return Collection.class.isAssignableFrom(fieldType) || Map.class.isAssignableFrom(fieldType);
  }

  protected CharSequence getColumnName(Field field) {
    Column column = field.getAnnotation(Column.class);
    if (column != null && !column.name().isEmpty()) {
      return column.name();
    }
    return toColumnName(field.getName());
  }

  protected CharSequence getTableName(Class<?> clazz) {
    Table table = clazz.getAnnotation(Table.class);
    if (table == null || table.name().isEmpty()) {
      return toTableName(clazz.getSimpleName());
    }
    return table.name();
  }

  protected Optional<String> catalogOrSchema(Class<?> clazz) {
    Table table = clazz.getAnnotation(Table.class);
    if (table != null) {
      String catalog = table.catalog();
      if (!catalog.isEmpty()) {
        return Optional.ofNullable(catalog);
      }
      String schema = table.schema();
      if (!schema.isEmpty()) {
        return Optional.ofNullable(schema);
      }
    }
    return Optional.empty();
  }

  protected String decapitalize(String src) {
    if (Character.isLowerCase(src.charAt(0))) {
      return src;
    }
    char[] dest = src.toCharArray();
    dest[0] = Character.toLowerCase(dest[0]);
    return new String(dest);
  }

  protected CharSequence toTableName(String javaName) {
    return camelToSnake(javaName);
  }

  protected CharSequence toColumnName(String javaName) {
    return camelToSnake(javaName);
  }

  protected CharSequence camelToSnake(String camelCase) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < camelCase.length(); i++) {
      char c = camelCase.charAt(i);
      if ('A' <= c && c <= 'Z') {
        if (i > 0) {
          result.append('_');
        }
        result.append((char) (c ^ 0x20));
      } else {
        result.append(c);
      }
    }
    return result;
  }

  protected List<Field> getFields(Class<?> clazz, Predicate<Field> test) {
    return fieldCache.computeIfAbsent(clazz, k -> {
      List<Field> fields = new ArrayList<>();
      collectFields(fields, clazz);
      return fields;
    }).stream().filter(test).collect(Collectors.toList());
  }

  protected void collectFields(List<Field> fields, Class<?> clazz) {
    if (clazz == null) {
      return;
    }
    fields.addAll(Arrays.stream(clazz.getDeclaredFields()).filter(f -> !Modifier.isStatic(f.getModifiers()))
        .collect(Collectors.toList()));
    collectFields(fields, clazz.getSuperclass());
  }

  public abstract StringBuilder upsert(Object bean, ProviderContext context);

  protected abstract CharSequence escape(CharSequence name);
}

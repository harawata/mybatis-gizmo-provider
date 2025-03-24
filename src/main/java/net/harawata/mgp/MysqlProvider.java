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
import java.util.List;

import org.apache.ibatis.builder.annotation.ProviderContext;

public class MysqlProvider extends GizmoProvider {
  protected static final String BACKTICK = "`";
  protected static final String NEWROW = "newrow";

  @Override
  protected CharSequence escape(CharSequence name) {
    return new StringBuilder().append(BACKTICK).append(name).append(BACKTICK);
  }

  @Override
  public StringBuilder upsert(Object bean, ProviderContext context) {
    Class<? extends Object> beanClass = bean.getClass();
    StringBuilder sql = insert(bean, context);
    List<Field> fields = getUpdatableFields(beanClass);
    sql.append(" as ").append(escape(NEWROW)).append(" on duplicate key update ");
    for (int i = 0; i < fields.size(); i++) {
      if (i > 0) {
        sql.append(", ");
      }
      sql.append(escape(getColumnName(fields.get(i)))).append(" = ").append(escape(NEWROW)).append(".")
          .append(escape(getColumnName(fields.get(i))));
    }
    return sql;
  }
}

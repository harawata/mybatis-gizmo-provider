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
package org.apache.ibatis.builder.annotation;

import static org.apache.ibatis.builder.annotation.MysqlProviderTest.paramMap;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import net.harawata.mgp.GizmoProvider;
import net.harawata.mgp.OracleProvider;

class OracleProviderTest {

  @Test
  void testInsert() throws Exception {
    GizmoProvider provider = new OracleProvider();
    assertEquals("insert into \"USER\" (\"ID\", \"NAME\") values (#{id}, #{name})", provider.insert(new User(),
        new ProviderContext(UserMapper.class, UserMapper.class.getMethod("insert", User.class), null)).toString());
  }

  @Test
  void testUpdate_MultiArgs() throws Exception {
    GizmoProvider provider = new OracleProvider();
    assertEquals("update \"USER\" set \"ID\" = #{user.id}, \"NAME\" = #{user.name} where \"ID\" = #{id}",
        provider.update(new ProviderContext(UserMapper.class,
            UserMapper.class.getMethod("update", User.class, Integer.class), null),
            paramMap("user", new User(), "id", 1)).toString());
  }

  @Test
  void testUpdateById_SingleArg_NoParam() throws Exception {
    GizmoProvider provider = new OracleProvider();
    assertEquals("update \"USER\" set \"ID\" = #{id}, \"NAME\" = #{name} where \"ID\" = #{id}", provider
        .update(new ProviderContext(UserMapper.class, UserMapper.class.getMethod("updateById", User.class),
            null), new User())
        .toString());
  }

  @Test
  void testUpdateByIdAndLastName_SingleArg_NoParam() throws Exception {
    GizmoProvider provider = new OracleProvider();
    assertEquals(
        "update \"USER\" set \"ID\" = #{id}, \"NAME\" = #{name} where \"ID\" = #{id} and \"LAST_NAME\" = #{lastName}",
        provider
            .update(
                new ProviderContext(UserMapper.class, UserMapper.class.getMethod("updateByIdAndLastName", User.class),
                    null),
                new User())
            .toString());
  }

  @Test
  void testUpdateById_SingleArg_WithParam() throws Exception {
    GizmoProvider provider = new OracleProvider();
    assertEquals("update \"USER\" set \"ID\" = #{user.id}, \"NAME\" = #{user.name} where \"ID\" = #{user.id}", provider
        .update(new ProviderContext(UserMapper.class, UserMapper.class.getMethod("updateById", User.class),
            null), paramMap("user", new User()))
        .toString());
  }

  @Test
  void testUpdateByIdAndLastName_SingleArg_WithParam() throws Exception {
    GizmoProvider provider = new OracleProvider();
    assertEquals(
        "update \"USER\" set \"ID\" = #{user.id}, \"NAME\" = #{user.name} where \"ID\" = #{user.id} and \"LAST_NAME\" = #{user.lastName}",
        provider.update(new ProviderContext(UserMapper.class,
            UserMapper.class.getMethod("updateByIdAndLastName", User.class), null),
            paramMap("user", new User())).toString());
  }

  @Test
  void testUpdateById_MultiArgs() throws Exception {
    GizmoProvider provider = new OracleProvider();
    assertEquals("update \"USER\" set \"ID\" = #{user.id}, \"NAME\" = #{user.name} where \"ID\" = #{id}", provider
        .update(
            new ProviderContext(UserMapper.class, UserMapper.class.getMethod("updateById", User.class, Integer.class),
                null),
            paramMap("user", new User(), "id", 1))
        .toString());
  }

  @Test
  void testUpdateByIdAndLastName_MultiArgs() throws Exception {
    GizmoProvider provider = new OracleProvider();
    assertEquals(
        "update \"USER\" set \"ID\" = #{user.id}, \"NAME\" = #{user.name} where \"ID\" = #{id} and \"LAST_NAME\" = #{lastName}",
        provider.update(
            new ProviderContext(UserMapper.class,
                UserMapper.class.getMethod("updateByIdAndLastName", User.class, Integer.class, String.class), null),
            paramMap("user", new User(), "id", 1, "lastName", "foo")).toString());
  }

  static interface UserMapper {
    void insert(User user);

    void update(User user, Integer id);

    void updateById(User user);

    void updateByIdAndLastName(User user);

    void updateById(User user, Integer id);

    void updateByIdAndLastName(User user, Integer id, String lastName);
  }

  static class User {
    private Integer id;
    private String name;

    public Integer getId() {
      return id;
    }

    public void setId(Integer id) {
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }
}

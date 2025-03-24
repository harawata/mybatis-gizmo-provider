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

import static org.junit.jupiter.api.Assertions.*;

import org.apache.ibatis.binding.MapperMethod.ParamMap;
import org.junit.jupiter.api.Test;

import net.harawata.mgp.GizmoProvider;
import net.harawata.mgp.MysqlProvider;

class MysqlProviderTest {

  @Test
  void testInsert() throws Exception {
    GizmoProvider provider = new MysqlProvider();
    assertEquals("insert into `user` (`id`, `name`) values (#{id}, #{name})", provider.insert(new User(),
        new ProviderContext(UserMapper.class, UserMapper.class.getMethod("insert", User.class), null)).toString());
  }

  @Test
  void testUpsert() throws Exception {
    GizmoProvider provider = new MysqlProvider();
    assertEquals(
        "insert into `user` (`id`, `name`) values (#{id}, #{name}) as `newrow` on duplicate key update `id` = `newrow`.`id`, `name` = `newrow`.`name`",
        provider.upsert(new User(),
            new ProviderContext(UserMapper.class, UserMapper.class.getMethod("upsert", User.class), null))
            .toString());
  }

  @Test
  void testUpdate_MultiArgs() throws Exception {
    GizmoProvider provider = new MysqlProvider();
    assertEquals("update `user` set `id` = #{user.id}, `name` = #{user.name} where `id` = #{id}",
        provider.update(new ProviderContext(UserMapper.class,
            UserMapper.class.getMethod("update", User.class, Integer.class), null),
            paramMap("user", new User(), "id", 1)).toString());
  }

  @Test
  void testUpdateById_SingleArg_NoParam() throws Exception {
    GizmoProvider provider = new MysqlProvider();
    assertEquals("update `user` set `id` = #{id}, `name` = #{name} where `id` = #{id}", provider
        .update(new ProviderContext(UserMapper.class, UserMapper.class.getMethod("updateById", User.class),
            null), new User())
        .toString());
  }

  @Test
  void testUpdateByIdAndLastName_SingleArg_NoParam() throws Exception {
    GizmoProvider provider = new MysqlProvider();
    assertEquals("update `user` set `id` = #{id}, `name` = #{name} where `id` = #{id} and `last_name` = #{lastName}",
        provider
            .update(
                new ProviderContext(UserMapper.class, UserMapper.class.getMethod("updateByIdAndLastName", User.class),
                    null),
                new User())
            .toString());
  }

  @Test
  void testUpdateById_SingleArg_WithParam() throws Exception {
    GizmoProvider provider = new MysqlProvider();
    assertEquals("update `user` set `id` = #{user.id}, `name` = #{user.name} where `id` = #{user.id}", provider
        .update(new ProviderContext(UserMapper.class, UserMapper.class.getMethod("updateById", User.class),
            null), paramMap("user", new User()))
        .toString());
  }

  @Test
  void testUpdateByIdAndLastName_SingleArg_WithParam() throws Exception {
    GizmoProvider provider = new MysqlProvider();
    assertEquals(
        "update `user` set `id` = #{user.id}, `name` = #{user.name} where `id` = #{user.id} and `last_name` = #{user.lastName}",
        provider.update(new ProviderContext(UserMapper.class,
            UserMapper.class.getMethod("updateByIdAndLastName", User.class), null),
            paramMap("user", new User())).toString());
  }

  @Test
  void testUpdateById_MultiArgs() throws Exception {
    GizmoProvider provider = new MysqlProvider();
    assertEquals("update `user` set `id` = #{user.id}, `name` = #{user.name} where `id` = #{id}", provider
        .update(
            new ProviderContext(UserMapper.class, UserMapper.class.getMethod("updateById", User.class, Integer.class),
                null),
            paramMap("user", new User(), "id", 1))
        .toString());
  }

  @Test
  void testUpdateByIdAndLastName_MultiArgs() throws Exception {
    GizmoProvider provider = new MysqlProvider();
    assertEquals(
        "update `user` set `id` = #{user.id}, `name` = #{user.name} where `id` = #{id} and `last_name` = #{lastName}",
        provider.update(
            new ProviderContext(UserMapper.class,
                UserMapper.class.getMethod("updateByIdAndLastName", User.class, Integer.class, String.class), null),
            paramMap("user", new User(), "id", 1, "lastName", "foo")).toString());
  }

  static ParamMap<Object> paramMap(Object... obj) {
    ParamMap<Object> paramMap = new ParamMap<>();
    for (int i = 0; i < obj.length; i += 2) {
      paramMap.put((String) obj[i], obj[i + 1]);
    }
    return paramMap;
  }

  static interface UserMapper {
    void insert(User user);

    void upsert(User user);

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

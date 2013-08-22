package nova.test.functional.master;

import java.util.List;

import nova.master.models.Users;

import org.junit.Test;

public class TestUsersDB {

    @Test
    public void testSave() {
        Users user = new Users();
        user.setName("root");
        user.setEmail("hechuan1215@gmail.com");
        user.setPassword("123456");
        user.setPrivilege("Root");
        user.setActivated("1");

        user.save();

        System.out.println(Users.last().toString());

    }

    @Test
    public void testShowAll() {
        List<Users> allusers = Users.all();
        for (Users user : allusers) {
            System.out.println(user.toString());
        }
    }

    @Test
    public void testfind() {
        Users us = Users.findById(9);
        System.out.println(us.toString());

        Users usr = Users.findByName("hechuan");
        System.out.println(usr.toString());
    }

    @Test
    public void testDelete() {
        // Users ur = Users.findById(5);
        System.out.println(Users.last().toString());
        Users.delete(Users.last());
        System.out.println("delete done!");
    }

}

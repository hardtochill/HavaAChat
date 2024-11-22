package cn.havaachat;

import java.lang.reflect.Field;

public class test {
    private static class Student{
        private String sno;

        public void setSno(String sno) {
            this.sno = sno;
        }
    }

    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        Student student1 = new Student();
        Student student2 = new Student();

        Field student1Sno = student1.getClass().getDeclaredField("sno");
        student1Sno.setAccessible(true);
        System.out.println(student1Sno.get(student1));

        student2.setSno("222");
        Field student2Sno = student1.getClass().getDeclaredField("sno");
        student2Sno.setAccessible(true);
        System.out.println(student2Sno.get(student2));
    }
}

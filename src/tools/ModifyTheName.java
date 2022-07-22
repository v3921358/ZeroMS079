/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author alienware
 */
public class ModifyTheName {

    /*public static void main(String[] args) {

        String path = "C:\\Users\\alienware\\Desktop\\新建文件夹 (5)";

        File file = new File(path);

        File[] array = file.listFiles();

        for (int i = 0; i < array.length; i++) {

            if (array[i].isFile()) {

                File person = array[i];

                String _fileName = person.getName();
                String sid = _fileName.substring(0, 8);
                int id = Integer.parseInt(sid);
                int zid = id - 50000;
                StringBuffer ic = new StringBuffer();

                ic.append("0").append(zid).append(_fileName.substring(8, 16));

                String newName = ic.toString();

                System.out.println("原来名字是：" + _fileName.substring(0, 8));
                System.out.println("原来名字后缀：" + _fileName.substring(8, 16));
                System.out.println("新名字是：" + newName);

                if (person.renameTo(new File(newName))) {

                    System.out.println("修改成功!");

                } else {

                    System.out.println("修改失败");

                }

            } else if (array[i].isDirectory()) {

                //getFile(array[i].getPath());
                System.out.println("错误了");

            }

        }

    }*/
    static String newString = "";//新字符串,如果是去掉前缀后缀就留空，否则写上需要替换的字符串
    static String oldString = "闭着眼睛飘单词";//要被替换的字符串
    static String dir = "C:\\Users\\alienware\\Desktop\\TamingMob";//文件所在路径，所有文件的根目录，记得修改为你电脑上的文件所在路径

    public static void main(String[] args) throws IOException {
        recursiveTraversalFolder(dir);//递归遍历此路径下所有文件夹
    }

    /**
     * 递归遍历文件夹获取文件
     */
    public static void recursiveTraversalFolder(String path) {
        File folder = new File(path);
        if (folder.exists()) {
            File[] fileArr = folder.listFiles();
            if (null == fileArr || fileArr.length == 0) {
                System.out.println("文件夹是空的!");
                return;
            } else {
                File newDir = null;//文件所在文件夹路径+新文件名
                String newName = "";//新文件名
                String fileName = null;//旧文件名
                File parentPath = new File("");//文件所在父级路径
                for (File file : fileArr) {
                    if (file.isDirectory()) {//是文件夹，继续递归，如果需要重命名文件夹，这里可以做处理
                        System.out.println("文件夹:" + file.getAbsolutePath() + "，继续递归！");
                        recursiveTraversalFolder(file.getAbsolutePath());
                    } else {//是文件，判断是否需要重命名
                        fileName = file.getName();
                        parentPath = file.getParentFile();
                        String sid = fileName.substring(0, 8);
                        int id = Integer.parseInt(sid);
                        int zid = id - 50000;
                        System.out.println("id：" + sid);
                        newName = "0" + zid + fileName.substring(8, 16);

                        newDir = new File(parentPath + "/" + newName);//文件所在文件夹路径+新文件名
                        file.renameTo(newDir);//重命名
                        System.out.println("修改后：" + newDir);

                    }
                }
            }
        } else {
            System.out.println("文件不存在!");
        }
    }
}

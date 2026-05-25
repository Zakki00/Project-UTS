package com.mycompany.projectuas;

import java.util.List;

public class test {

    public static void main(String[] args) {

        String sql = "SELECT * FROM tb_transaksi ORDER BY id_transaksi DESC LIMIT 1";

        List<Object[]> data = koneksi.ambilData(sql);

        for (Object[] row : data) {
            int id_transaski = ((Number) row[0]).intValue();
            int id_user = ((Number) row[1]).intValue();
            
            int poin = ((Number) row[2]).intValue();

            System.out.println("ID: " + id_transaski);
            System.out.println("Nama Hadiah: " + id_user);
            System.out.println("Poin: " + poin);
            System.out.println("-------------------");
        }
    }
}
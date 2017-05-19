/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sdxess;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import static sdxess.ExecutorTask.setTimeout;

/**
 *
 * @author kotaro
 */
public class HostEdit extends javax.swing.JFrame {
    private javax.swing.table.DefaultTableModel tableModel;
    private StringBuilder sb = new StringBuilder();
    private BufferedReader br = null;
    private ArrayList<Website> websites;
    
   
    
    public HostEdit(ArrayList<Website> websites)  {
        initComponents();
        
        this.websites = websites;
        this.tableModel = new javax.swing.table.DefaultTableModel(
            new Object [][] {
            },
            new String [] {
                "Website" , "Rerouted IP"
            }
        );
        
        for( int i = 0 ; i < this.websites.size() ; i++ ){
            Website website = this.websites.get(i);
            this.tableModel.addRow(new Object[]{ website.name , website.IP });
        }
        
        sitesTable.setModel(this.tableModel);

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        addBtn = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        sitesTable = new javax.swing.JTable();
        delBtn = new javax.swing.JButton();
        editBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Websites");

        addBtn.setText("Add Website");
        addBtn.setEnabled(false);
        addBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addBtnActionPerformed(evt);
            }
        });

        sitesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(sitesTable);

        delBtn.setText("Delete Website");
        delBtn.setEnabled(false);
        delBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                delBtnActionPerformed(evt);
            }
        });

        editBtn.setText("Edit Website");
        editBtn.setEnabled(false);
        editBtn.setName("Websites"); // NOI18N
        editBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(addBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(editBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(delBtn))
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 497, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 186, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addBtn)
                    .addComponent(delBtn)
                    .addComponent(editBtn)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    
    public static void restartDNS(){
        try {
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "" );
            Process process;
        
            process = builder.start();      

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line="";

            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        
        } catch (IOException ex) {
            Logger.getLogger(HostEdit.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void delBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delBtnActionPerformed
        if( sitesTable.getSelectedRow() != -1 ){
            String name = (String) tableModel.getValueAt(sitesTable.getSelectedRow(), 0);
            tableModel.removeRow(sitesTable.getSelectedRow());
            for( int i = 0 ; i < this.websites.size() ; i++ ){
                if( this.websites.get(i).name.compareTo(name) == 0 ){
                    try {
                        StaticRoutes.deleteStaticRoute(this.websites.get(i).IP);
                        this.websites.remove(i);
                        i = this.websites.size();
                    } catch (IOException ex) {
                        Logger.getLogger(HostEdit.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }//GEN-LAST:event_delBtnActionPerformed

    private void addBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addBtnActionPerformed
        Website website = new Website("8vg.org");
        this.websites.add(website);
        this.tableModel.addRow(new Object[]{ website.name , website.IP });
        try {
            StaticRoutes.AddStaticRoute(website.IP);
        } catch (IOException ex) {
            Logger.getLogger(HostEdit.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_addBtnActionPerformed

    private void editBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editBtnActionPerformed
        int index = sitesTable.getSelectedRow();
        if( index == -1 )
            return;
        
        String name = (String)tableModel.getValueAt(index, 0);
        Website website = null;
        
        for( int i = 0 ; i < this.websites.size() ; i++ ){
            if( this.websites.get(i).name.compareTo(name) == 0 ){
                website = this.websites.get(i);
            }
        }
        
        if( website == null )
            return;
        
        String new_name = JOptionPane.showInputDialog(
            this, 
            "Change Website Name (" + name + ") to:", 
            name
        );
        
        try {
            website.name = new_name;
            StaticRoutes.deleteStaticRoute(website.IP);
            
            if( Website.isIP(website.name) ){
                website.IP = Website.getClassB( name );
                website.isStatic = true;
            }else{
                website.isStatic = false;
                website.updateIp();
            }
            
            tableModel.setValueAt(website.name, index, 0);
            tableModel.setValueAt(website.IP, index, 1);
            StaticRoutes.AddStaticRoute(website.IP);
            
        } catch (IOException ex) {
            Logger.getLogger(HostEdit.class.getName()).log(Level.SEVERE, null, ex);
        }
       
    }//GEN-LAST:event_editBtnActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(HostEdit.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(HostEdit.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(HostEdit.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(HostEdit.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

   
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addBtn;
    private javax.swing.JButton delBtn;
    private javax.swing.JButton editBtn;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable sitesTable;
    // End of variables declaration//GEN-END:variables
}

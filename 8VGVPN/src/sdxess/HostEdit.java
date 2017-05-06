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
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author kotaro
 */
public class HostEdit extends javax.swing.JFrame {
    private javax.swing.table.DefaultTableModel tableModel;
    private StringBuilder sb = new StringBuilder();
    private BufferedReader br = null;
    
    public HostEdit()  {
        initComponents();
        
        this.tableModel = new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"facebook.com","31.13.95.36"},
                {"google.com.tw","202.169.0.0"}
            },
            new String [] {
                "Website", "IP address"
            }
        );
        
        jTable1.setModel(this.tableModel);
        try {
            br = new BufferedReader(new FileReader("C:/Windows/System32/drivers/etc/hosts"));
            
            String line = br.readLine();

            while (line != null) {
                if ( line.contains("#") || line.trim().isEmpty() ){
                    sb.append(line);
                    sb.append(System.lineSeparator());
                }else{
                    String[] instruction = line.replaceAll("(\\s+)|(\t+)", " ").split(" ");
                    //this.tableModel.addRow(new Object []{ instruction[1] , instruction[0]});
                }
                line = br.readLine();
            }
            String everything = sb.toString();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(HostEdit.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(HostEdit.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(HostEdit.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        SetRoutesButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jButton1.setText("Add Row");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Save Table");
        jButton2.setEnabled(false);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(jTable1);

        jButton3.setText("Delete Row");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setText("Edit Row");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        SetRoutesButton.setText("Set Routes");
        SetRoutesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SetRoutesButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton4)
                .addGap(18, 18, 18)
                .addComponent(SetRoutesButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton2))
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 470, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2)
                    .addComponent(jButton3)
                    .addComponent(jButton4)
                    .addComponent(SetRoutesButton)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        String file = sb.toString(); 
        for( int i = 0 ; i < this.tableModel.getRowCount() ; i++ ){
            file += this.tableModel.getValueAt(i, 1) + "\t" + this.tableModel.getValueAt(i, 0) + System.lineSeparator();
        }
        try {
            PrintWriter out = new PrintWriter("C:/Windows/System32/drivers/etc/hosts");
            out.println(file);
            out.close();
            System.out.println(file);
            JOptionPane.showMessageDialog(null, "Changes have been saved!");
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(null, "You need to run this program as administrator");
        } 
        
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        if( jTable1.getSelectedRow() != -1 ){
            tableModel.removeRow(jTable1.getSelectedRow());
            System.out.println(tableModel.getRowCount());
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        tableModel.addRow(new Object []{"8vg.net" , "127.0.0.1"});
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        int index = jTable1.getSelectedRow();
        if( index == -1 )
            return;
        
        String website = JOptionPane.showInputDialog(
            this, 
            "Change Website (" + (String)tableModel.getValueAt(index, 0) + ") to:", 
            (String)tableModel.getValueAt(index, 0)
        );

        
        if ( website != null ){
    
            String redirect = JOptionPane.showInputDialog(
                this, 
                "Change redirect (" + (String)tableModel.getValueAt(index, 1) + ") to:", 
                (String)tableModel.getValueAt(index, 1)
            );

            tableModel.setValueAt(website, index, 0);
            tableModel.setValueAt(redirect, index, 1);
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void SetRoutesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SetRoutesButtonActionPerformed
        // TODO add your handling code here:
            StaticRoutes SR = new StaticRoutes();
            int columns = tableModel.getColumnCount();
            int rows = tableModel.getRowCount();
            String website;
            for (int i = 0; i < rows; i++) {
                for (int y = 0; y < 1; y++) {
                    website = tableModel.getValueAt(i, y).toString();
                    System.out.println("Website " + i +": "+website);
                    try {
                        SR.AddStaticRoute(SR.NSLookup(website), SR.GetTAPInfo(11), SR.GetTAPInfo(6));
                    } catch (IOException ex) {
                        Logger.getLogger(HostEdit.class.getName()).log(Level.SEVERE, null, ex);
                    }
            }
            //return result;
       }
    }//GEN-LAST:event_SetRoutesButtonActionPerformed

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
    private javax.swing.JButton SetRoutesButton;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}

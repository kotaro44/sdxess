/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sdxess;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author kotaro
 */
public class HostEdit extends javax.swing.JFrame {
    private javax.swing.table.DefaultTableModel tableModel;
    private StringBuilder sb = new StringBuilder();
    private BufferedReader br = null;
    private ArrayList<Website> websites;
    private String windowName = "Websites";
    public static ACType accountType = ACType.NOTYPE;
    
   
    /***************************************************************************
    ***  brief                                                               ***
    ***  serial number ????                                                  ***
    ***  parameter out <none>                                                ***
    ***  parameter in  <none>                                                ***
    ***  return <none>                                                       ***
    *** @param                                                               ***
    ***************************************************************************/
    public HostEdit(ArrayList<Website> websites)  {
        initComponents();
        URL iconURL = getClass().getResource("/sdxess/icon.png");
        ImageIcon icon = new ImageIcon(iconURL);
        alertIcoLbl.setVisible(false);
        alertMsgLbl.setVisible(false);
        loadingIconLbl.setVisible(false);
      
        if( HostEdit.accountType == ACType.STARTER ){
            addBtn.setVisible(false);
            delBtn.setVisible(false);
            redirectCheck.setVisible(false);
        }else{
            updateLbl.setVisible(false);
        }
        
        this.setIconImage(icon.getImage());
        this.windowName = this.getTitle();
        this.websites = websites;
        this.updateTable();

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        updateLbl = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        sitesTable = new javax.swing.JTable();
        alertIcoLbl = new javax.swing.JLabel();
        loadingIconLbl = new javax.swing.JLabel();
        addBtn = new javax.swing.JButton();
        delBtn = new javax.swing.JButton();
        detailBtn = new javax.swing.JButton();
        alertMsgLbl = new javax.swing.JLabel();
        redirectCheck = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Websites");
        addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                formPropertyChange(evt);
            }
        });

        updateLbl.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        updateLbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        updateLbl.setText("<html> <a href=\"\">Upgrade your account!</a></html>");
        updateLbl.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        updateLbl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                updateLblMouseClicked(evt);
            }
        });

        sitesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null,  new Boolean(false), null}
            },
            new String [] {
                "Website", "Routed IP", "Routed", "Description"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.Boolean.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, true, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        sitesTable.setEditingColumn(0);
        sitesTable.setEditingRow(0);
        sitesTable.setRowSelectionAllowed(false);
        sitesTable.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                sitesTablePropertyChange(evt);
            }
        });
        jScrollPane1.setViewportView(sitesTable);
        if (sitesTable.getColumnModel().getColumnCount() > 0) {
            sitesTable.getColumnModel().getColumn(0).setPreferredWidth(100);
            sitesTable.getColumnModel().getColumn(1).setPreferredWidth(100);
            sitesTable.getColumnModel().getColumn(2).setPreferredWidth(50);
            sitesTable.getColumnModel().getColumn(3).setPreferredWidth(400);
        }

        alertIcoLbl.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sdxess/alert.png"))); // NOI18N

        loadingIconLbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        loadingIconLbl.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sdxess/loader.gif"))); // NOI18N
        loadingIconLbl.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        addBtn.setText("Add Website");
        addBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        addBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addBtnActionPerformed(evt);
            }
        });

        delBtn.setText("Delete Website");
        delBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        delBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                delBtnActionPerformed(evt);
            }
        });

        detailBtn.setText("details");
        detailBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        detailBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                detailBtnActionPerformed(evt);
            }
        });

        alertMsgLbl.setText("Remember to delete the cache of your browser!");
        alertMsgLbl.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        redirectCheck.setText("redirect All Traffic");
        redirectCheck.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        redirectCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                redirectCheckActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
            .addGroup(layout.createSequentialGroup()
                .addComponent(addBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(delBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(detailBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(loadingIconLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(updateLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(alertIcoLbl)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(alertMsgLbl)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 144, Short.MAX_VALUE)
                .addComponent(redirectCheck))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(redirectCheck)
                        .addComponent(updateLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(alertMsgLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(alertIcoLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(addBtn)
                        .addComponent(delBtn)
                        .addComponent(detailBtn))
                    .addComponent(loadingIconLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /***************************************************************************
    ***  brief                                                               ***
    ***  serial number ????                                                  ***
    ***  parameter out <none>                                                ***
    ***  parameter in  <none>                                                ***
    ***  return <none>                                                       ***
    *** @param                                                               ***
    ***************************************************************************/
    public static void restartDNS(){
        try {
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "" );
            Process process;
        
            process = builder.start();      

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line="";

            while ((line = reader.readLine()) != null) {
                Console.log(line);
            }
        
        } catch (IOException ex) {
            Logger.getLogger(HostEdit.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void updateTable(){
        this.tableModel = (DefaultTableModel) sitesTable.getModel();
        while( this.tableModel.getRowCount() != 0 ){
            this.tableModel.removeRow(0);
        }
        
        for( int i = 0 ; i < this.websites.size() ; i++ ){
            Website website = this.websites.get(i);
            this.tableModel.addRow(new Object[]{ website.name , website.IP , website.isRouted() , website.Description });
        }

        sitesTable.setModel(this.tableModel);
    }
    
    private void delBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delBtnActionPerformed
        if( sitesTable.getSelectedRow() != -1 ){
            int dialogResult = JOptionPane.showConfirmDialog (null, 
                    "Are you sure you want to delete the selected Websites?",
                    "Warning",JOptionPane.YES_NO_OPTION);
            if(dialogResult == JOptionPane.YES_OPTION){
                this.disableWindow();
                this.deleteWebsiteAsync();
            }  
        }
    }//GEN-LAST:event_delBtnActionPerformed

    public void deleteWebsiteAsync(){
        int indexes[] = sitesTable.getSelectedRows();
        ArrayList<Website> websites2remove = new ArrayList<Website>();
        for( int index : indexes ){
            websites2remove.add(this.websites.get(index));
        }
        for( Website website : websites2remove ){
            this.message("Unrouting " + website.name + "...");
            website.deleteRouting();
        }
        this.showSuggestion();
        this.websites.removeAll(websites2remove);
        this.updateTable();
        this.message(null);
        this.saveWebsites();
        this.enableWindow(false);
    }
    
    public void disableWindow(){
        this.disableWindow(false);
    }
    
    public void enableWindow(){
        this.enableWindow(false);
    }
    
    public void disableWindow( boolean byAlltraffic ){
        addBtn.setEnabled(false);
        delBtn.setEnabled(false);
        detailBtn.setEnabled(false);
        sitesTable.setEnabled(false);
        sitesTable.setBackground(new java.awt.Color(204, 204, 204));
        if( !byAlltraffic ){
            loadingIconLbl.setVisible(true);
            redirectCheck.setEnabled(false);
        }
    }
    
    public void enableWindow( boolean byAlltraffic ){
        addBtn.setEnabled(true);
        delBtn.setEnabled(true);
        detailBtn.setEnabled(true);
        sitesTable.setEnabled(true);
        sitesTable.setBackground(new java.awt.Color(255, 255, 255));
        if( !byAlltraffic ){
            loadingIconLbl.setVisible(false);
            redirectCheck.setEnabled(true);
        }
    }
    
    
    private void addBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addBtnActionPerformed
        String domain = JOptionPane.showInputDialog(this, "Enter Domain, IP or ASN number" , "8vg.org");
        if( domain != null ){
            if ( Website.isValidDomainName(domain) || Website.isASNNumber(domain) || 
                    Website.isIP(domain)) {
                
                String description = JOptionPane.showInputDialog(this, "Enter a description" , domain );
                if( description == null ) {
                    description = "";
                }
                this.addWebsite(domain,description);
            } else {
                JOptionPane.showMessageDialog(this, domain + " is not a valid Domain name, IP or ASN number", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_addBtnActionPerformed

    private void detailBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_detailBtnActionPerformed
         if( sitesTable.getSelectedRow() != -1 ){
            Website website = this.websites.get(sitesTable.getSelectedRow());
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    new details(website).setVisible(true);
                }
            });
        }
    }//GEN-LAST:event_detailBtnActionPerformed

    private void sitesTablePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_sitesTablePropertyChange
        if( evt.getPropertyName().compareTo("tableCellEditor") == 0 ){
            boolean shouldSave = false;
            if( this.websites != null && this.tableModel != null){
                for( int i = 0 ; i < this.websites.size() ; i++ ){
                    Website website = this.websites.get(i);
                    if( (boolean)this.tableModel.getValueAt(i,2) != website.isRouted() ){
                        shouldSave = true;
                        this.disableWindow();
                        if( website.isRouted() ){
                            this.message("Unrouting " + website.name + "...");
                            website.deleteRouting();
                        }else{
                            this.message("Routing " + website.name + "...");
                            website.route();
                        }

                        this.message(null);
                        this.enableWindow();
                        this.showSuggestion();
                    }
                }
            }
            if( shouldSave ){
                this.saveWebsites();
            }
        }
    }//GEN-LAST:event_sitesTablePropertyChange

    private void formPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_formPropertyChange
        
    }//GEN-LAST:event_formPropertyChange

    private void updateLblMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_updateLblMouseClicked
        Website.openSDXWebsite();
    }//GEN-LAST:event_updateLblMouseClicked

    private void redirectCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_redirectCheckActionPerformed
        this.showSuggestion();
        if( this.redirectCheck.isSelected() ){
            this.disableWindow(true);
            StaticRoutes.enableAllTrafficReroute();
        }else{
            this.enableWindow(true);
            StaticRoutes.disableAllTrafficReroute();
        }
    }//GEN-LAST:event_redirectCheckActionPerformed
    
    public void addWebsite(String domain, String description){
        this.message("getting info from " + domain + "...");
        this.disableWindow();
        ExecutorTask.setTimeout(()-> this.addWebsiteAsync(domain,description), 10);
    }
    
    public void showSuggestion(){
        alertIcoLbl.setVisible(true);
        alertMsgLbl.setVisible(true);
    }
  
    public static ArrayList<Website> restoreWebsites(){
        ArrayList<Website> result = new ArrayList<>();
        String folder = "acbase";
        if( HostEdit.accountType == ACType.STARTER )
            folder = "actwo";
        if( HostEdit.accountType == ACType.ADVANCED )
            folder = "websites";
        
        File website_folder = new File(folder);
        if( website_folder.exists() ){
            File[] listOfFiles = website_folder.listFiles();
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    Console.log("restoring " + file.getName() + "...");
                    Website website = Website.restore(file);
                    result.add(website);
                } 
            }
        }
        return result;
    }
    
    private void addWebsiteAsync(String domain,String description){
        Website website = new Website(domain,this);
        if( website.isValid ){
            website.Description = description;
            boolean addNewWebsite = true;
            //check if the website was added before
            for( Website other : this.websites ){
                if( other.ASN.compareTo(website.ASN) == 0 ){
                    JOptionPane.showMessageDialog(null, "\"" +  website.name + 
                            "\" IP's are already included in \"" + other.name + 
                            "\" (" + other.ASN + ")", "ErrorMsg" , 
                            JOptionPane.ERROR_MESSAGE);
                    addNewWebsite = false;
                }
            }
            
            if( addNewWebsite ){
                this.websites.add(website);
                this.updateTable();
                this.saveWebsites();
            }
        }else{
            JOptionPane.showMessageDialog(this, domain + " not found!", "Error", JOptionPane.ERROR_MESSAGE);
        }
        this.message(null);
        this.enableWindow();
    }
    
    
    public static void deleteDir(String path) {
        try {
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "rmdir -r " + path + " /s /q");
            builder.redirectErrorStream(true);
            Process p;
            p = builder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = "";
            int flag=0;
            while (flag == 0) {
                line = r.readLine();
                if (line == null) {
                    flag = 1;
                    break; 
                }
                Console.log(line);
            }
        } catch (IOException ex) {
            Logger.getLogger(HostEdit.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void saveWebsites(){
        HostEdit.saveWebsites(this.websites);
    }
    
    public static void saveWebsites(ArrayList<Website> websites){
        Console.log("saving websites!");
        
        String folder = "acbase";
        if( HostEdit.accountType == ACType.STARTER )
            folder = "actwo";
        if( HostEdit.accountType == ACType.ADVANCED )
            folder = "websites";
        
        File theDir = new File(folder);
        if( theDir.exists() ){
            deleteDir(folder);
        } 
        theDir = new File(folder);
        theDir.mkdir();
        
        try{
            for( Website website : websites ){
                PrintWriter writer = new PrintWriter(folder + "\\" + website.name, "UTF-8");
                writer.println(website.ASN);
                writer.println(website.IP);
                writer.println(website.isRouted());
                writer.println(website.Description.replace('\n', ' '));
                for( IPRange range : website.ranges ){
                    writer.println(range.toString(true));
                }
                writer.close();
            }
        } catch (IOException e) {
           // do something
        }
 
    }
    
    public void message(String message){
        if( message != null )
            this.setTitle( this.windowName + " - ( " + message + " )");
        else
            this.setTitle( this.windowName );
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addBtn;
    private javax.swing.JLabel alertIcoLbl;
    private javax.swing.JLabel alertMsgLbl;
    private javax.swing.JButton delBtn;
    private javax.swing.JButton detailBtn;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel loadingIconLbl;
    private javax.swing.JCheckBox redirectCheck;
    private javax.swing.JTable sitesTable;
    private javax.swing.JLabel updateLbl;
    // End of variables declaration//GEN-END:variables
}

package org.ruyisdk.devices.model;

public class Device {
    private String name;
    private String chip;
    private String vendor;
    private String version;
    private boolean isDefault;

    public Device(String name, String chip, String vendor, String version, boolean isDefault) {
        this.name = name;
        this.chip = chip;
        this.vendor = vendor;
        this.version = version;
        this.isDefault = isDefault;
    }
    
    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getChip() { return chip; }
    public void setChip(String chip) { this.chip = chip; }
    
    public String getVendor() { return vendor; }
    public void setVendor(String vendor) { this.vendor = vendor; }
    
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    
    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }
    


//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj) return true;
//        if (obj == null || getClass() != obj.getClass()) return false;
//        Board board = (Board) obj;
//        return name.equals(board.name) &&
//               chip.equals(board.chip) &&
//               vendor.equals(board.vendor) &&
//               version.equals(board.version);
//    }
}
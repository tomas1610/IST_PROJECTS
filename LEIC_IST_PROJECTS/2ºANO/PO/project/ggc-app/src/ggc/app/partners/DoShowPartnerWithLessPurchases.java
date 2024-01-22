package ggc.app.partners;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import ggc.Warehouse;
import ggc.WarehouseManager;

public class DoShowPartnerWithLessPurchases extends Command<WarehouseManager>{

    public DoShowPartnerWithLessPurchases(WarehouseManager receiver){
        super(Label.LESS_SALES,receiver);
    }

    public void execute(){
        _display.popup(_receiver.showLessPurchases());
    }
}
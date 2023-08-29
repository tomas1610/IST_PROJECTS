package ggc.app.partners;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import ggc.exceptions.UnknownPartnerException;
import ggc.app.exceptions.UnknownPartnerKeyException;
import java.util.ArrayList;
import ggc.WarehouseManager;
//FIXME import classes

/**
 * Show all transactions for a specific partner.
 */
class DoShowPartnerSales extends Command<WarehouseManager> {

  DoShowPartnerSales(WarehouseManager receiver) {
    super(Label.SHOW_PARTNER_SALES, receiver);
    addStringField("partnerKey",Prompt.partnerKey());
  }

  @Override
  public void execute() throws CommandException {
    try {
      String key = stringField("partnerKey");
      ArrayList<String> lista = _receiver.showPartnerSales(key);
      for (String s : lista){
        _display.popup(s);
      }
    }
    catch (UnknownPartnerException e){
      throw new UnknownPartnerKeyException(e.getKey());
    }
  }

}

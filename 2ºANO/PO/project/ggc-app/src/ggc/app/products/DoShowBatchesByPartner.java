package ggc.app.products;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import ggc.app.exceptions.UnknownPartnerKeyException;
import ggc.exceptions.UnknownPartnerException;
import java.util.ArrayList;
import ggc.WarehouseManager;
//FIXME import classes

/**
 * Show batches supplied by partner.
 */
class DoShowBatchesByPartner extends Command<WarehouseManager> {

  DoShowBatchesByPartner(WarehouseManager receiver) {
    super(Label.SHOW_BATCHES_SUPPLIED_BY_PARTNER, receiver);
    addStringField("partnerKey",Prompt.partnerKey());
  }

  @Override
  public final void execute() throws CommandException {
    try {
      String p = stringField("partnerKey");
      ArrayList<String> lista = _receiver.showBatchesByPartner(p);
      for (String s:lista){
        _display.popup(s);
      }
    }
    catch (UnknownPartnerException e){
      throw new UnknownPartnerKeyException(e.getKey());
    }
    
  }

}

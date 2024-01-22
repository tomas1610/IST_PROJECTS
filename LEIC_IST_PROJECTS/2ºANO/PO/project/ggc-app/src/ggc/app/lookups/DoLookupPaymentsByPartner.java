package ggc.app.lookups;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import ggc.app.exceptions.UnknownPartnerKeyException;
import ggc.exceptions.UnknownPartnerException;
import java.util.ArrayList;
import ggc.WarehouseManager;
//FIXME import classes

/**
 * Lookup payments by given partner.
 */
public class DoLookupPaymentsByPartner extends Command<WarehouseManager> {

  public DoLookupPaymentsByPartner(WarehouseManager receiver) {
    super(Label.PAID_BY_PARTNER, receiver);
    addStringField("partnerId",Prompt.partnerKey());
  }

  @Override
  public void execute() throws CommandException {
    try {
      String partnerId = stringField("partnerId");
      ArrayList<String> lista = new ArrayList<String>();
      lista = _receiver.showPaymentsByPartner(partnerId);
      for (String s : lista){
        _display.popup(s);
      }
    }
    catch (UnknownPartnerException e){
      throw new UnknownPartnerKeyException(e.getKey());
    }
  }
}

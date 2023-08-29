package ggc.app.partners;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import ggc.exceptions.ShowPartnerException;
import ggc.app.exceptions.UnknownPartnerKeyException;
import ggc.WarehouseManager;
import java.util.ArrayList;
//FIXME import classes

/**
 * Show partner.
 */
class DoShowPartner extends Command<WarehouseManager> {

  DoShowPartner(WarehouseManager receiver) {
    super(Label.SHOW_PARTNER, receiver);
    addStringField("partnerKey",Prompt.partnerKey());
  }

  @Override
  public void execute() throws CommandException {
    try {
      String key = stringField("partnerKey");
      String text = _receiver.showPartner(key);
      ArrayList<String> notifications = _receiver.showNotificationsPartner(key);
      _display.popup(text);
      if (notifications != null){
        for (String s : notifications){
          _display.popup(s);
        }
        _receiver.clearNotifications(key);
      }
    }
    catch (ShowPartnerException e){
      throw new UnknownPartnerKeyException(e.getKey());
    }
  }

}

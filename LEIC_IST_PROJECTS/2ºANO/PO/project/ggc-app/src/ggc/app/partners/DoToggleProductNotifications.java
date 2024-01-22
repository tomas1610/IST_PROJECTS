package ggc.app.partners;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import ggc.exceptions.UnknownProductException;
import ggc.app.exceptions.UnknownProductKeyException;
import ggc.exceptions.UnknownPartnerException;
import ggc.app.exceptions.UnknownPartnerKeyException;
import java.util.ArrayList;
import ggc.WarehouseManager;
//FIXME import classes

/**
 * Toggle product-related notifications.
 */
class DoToggleProductNotifications extends Command<WarehouseManager> {

  DoToggleProductNotifications(WarehouseManager receiver) {
    super(Label.TOGGLE_PRODUCT_NOTIFICATIONS, receiver);
    addStringField("partnerKey",Prompt.partnerKey());
    addStringField("productKey",Prompt.productKey());
  }

  @Override
  public void execute() throws CommandException {
    try {
      String p1 = stringField("partnerKey");
      String p2 = stringField("productKey");
      _receiver.toggleNotifications(p1,p2);
    }
    catch (UnknownPartnerException e){
      throw new UnknownPartnerKeyException(e.getKey());
    }
    catch (UnknownProductException e){
      throw new UnknownProductKeyException(e.getKey());
    }
  }
}

package ggc.app.transactions;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import ggc.exceptions.UnknownPartnerException;
import ggc.app.exceptions.UnknownPartnerKeyException;
import ggc.exceptions.UnknownProductException;
import ggc.app.exceptions.UnknownProductKeyException;
import ggc.exceptions.UnavailableProductUIException;
import ggc.app.exceptions.UnavailableProductException;
import ggc.WarehouseManager;
//FIXME import classes

/**
 * Register order.
 */
public class DoRegisterBreakdownTransaction extends Command<WarehouseManager> {

  public DoRegisterBreakdownTransaction(WarehouseManager receiver) {
    super(Label.REGISTER_BREAKDOWN_TRANSACTION, receiver);
    addStringField("partnerId",Prompt.partnerKey());
    addStringField("productId",Prompt.productKey());
    addIntegerField("amount",Prompt.amount());
  }

  @Override
  public final void execute() throws CommandException {
    try {
      String partnerId = stringField("partnerId");
      String productId = stringField("productId");
      int amount = integerField("amount");
      _receiver.registBreakDown(partnerId,productId,amount);
    }
    catch (UnknownPartnerException e){
      throw new UnknownPartnerKeyException(e.getKey());
    }
    catch (UnknownProductException e){
      throw new UnknownProductKeyException(e.getKey());
    }
    catch (UnavailableProductUIException e){
      throw new UnavailableProductException(e.getKey(), e.getRequested(), e.getAvailable());
    }
  }
}

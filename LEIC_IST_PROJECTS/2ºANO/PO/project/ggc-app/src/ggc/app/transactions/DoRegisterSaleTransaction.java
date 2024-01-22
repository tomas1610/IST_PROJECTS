package ggc.app.transactions;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import ggc.app.exceptions.UnavailableProductException;
import ggc.exceptions.UnavailableProductUIException;
import ggc.app.exceptions.UnknownPartnerKeyException;
import ggc.exceptions.UnknownPartnerException;
import ggc.app.exceptions.UnknownProductKeyException;
import ggc.exceptions.UnknownProductException;
import ggc.WarehouseManager;

/**
 * 
 */
public class DoRegisterSaleTransaction extends Command<WarehouseManager> {

  public DoRegisterSaleTransaction(WarehouseManager receiver) {
    super(Label.REGISTER_SALE_TRANSACTION, receiver);
    addStringField("partnerKey",Prompt.partnerKey());
    addIntegerField("date",Prompt.paymentDeadline());
    addStringField("productKey",Prompt.productKey());
    addIntegerField("amount",Prompt.amount());
  }

  @Override
  public final void execute() throws CommandException {
    try {
      String partnerKey = stringField("partnerKey");
      int date = integerField("date");
      String productKey = stringField("productKey");
      int amount = integerField("amount");
      _receiver.registeSale(partnerKey,date,productKey,amount);
    }
    catch (UnavailableProductUIException e){
      throw new UnavailableProductException(e.getKey(),e.getRequested(),e.getAvailable());
    }
    catch (UnknownPartnerException e){
      throw new UnknownPartnerKeyException(e.getKey());
    }
    catch (UnknownProductException e){
      throw new UnknownProductKeyException(e.getKey());
    }
  }
}

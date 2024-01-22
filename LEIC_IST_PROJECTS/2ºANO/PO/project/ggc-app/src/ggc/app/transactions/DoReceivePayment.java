package ggc.app.transactions;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import ggc.exceptions.UnknownTransactionException;
import ggc.app.exceptions.UnknownTransactionKeyException;
import ggc.WarehouseManager;
//FIXME import classes

/**
 * Receive payment for sale transaction.
 */
public class DoReceivePayment extends Command<WarehouseManager> {

  public DoReceivePayment(WarehouseManager receiver) {
    super(Label.RECEIVE_PAYMENT, receiver);
    addIntegerField("transactionKey",Prompt.transactionKey());
  }

  @Override
  public final void execute() throws CommandException {
    try {
      int key = integerField("transactionKey");
      _receiver.pay(key);
    }
    catch (UnknownTransactionException e){
      throw new UnknownTransactionKeyException(e.getKey());
    }
  }

}

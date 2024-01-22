package ggc.app.transactions;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import ggc.app.exceptions.UnknownTransactionKeyException;
import ggc.exceptions.UnknownTransactionException;
import ggc.WarehouseManager;

/**
 * Show specific transaction.
 */
public class DoShowTransaction extends Command<WarehouseManager> {

  public DoShowTransaction(WarehouseManager receiver) {
    super(Label.SHOW_TRANSACTION, receiver);
    addIntegerField("key",Prompt.transactionKey());
  }

  @Override
  public final void execute() throws CommandException {
    try {
      int key = integerField("key");
      String text = _receiver.showTransaction(key);
      _display.popup(text);
    }
    catch (UnknownTransactionException e){
      throw new UnknownTransactionKeyException(e.getKey());
    }
  }
}

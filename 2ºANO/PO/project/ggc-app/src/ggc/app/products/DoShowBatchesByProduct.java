package ggc.app.products;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import ggc.exceptions.UnknownProductException;
import ggc.app.exceptions.UnknownProductKeyException;
import java.util.ArrayList;
import ggc.WarehouseManager;
//FIXME import classes

/**
 * Show all products.
 */
class DoShowBatchesByProduct extends Command<WarehouseManager> {

  DoShowBatchesByProduct(WarehouseManager receiver) {
    super(Label.SHOW_BATCHES_BY_PRODUCT, receiver);
    addStringField("productKey",Prompt.productKey());
  }

  @Override
  public final void execute() throws CommandException {
    try {
      String p = stringField("productKey");
      ArrayList<String> lista = _receiver.showBatchesByProduct(p);
      for (String s:lista){
        _display.popup(s);
      }
    }
    catch (UnknownProductException e){
      throw new UnknownProductKeyException(e.getKey());
    }
  }

}

package ggc.app.lookups;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import ggc.app.exceptions.UnknownProductKeyException;
import ggc.exceptions.UnknownProductException;
import java.util.ArrayList;
import ggc.WarehouseManager;

/**
 * Lookup products cheaper than a given price.
 */
public class DoLookupProductBatchesUnderGivenPrice extends Command<WarehouseManager> {

  public DoLookupProductBatchesUnderGivenPrice(WarehouseManager receiver) {
    super(Label.PRODUCTS_UNDER_PRICE, receiver);
    addRealField("priceLimit",Prompt.priceLimit());
  }

  @Override
  public void execute() throws CommandException {
    Double priceLimit = realField("priceLimit");
    ArrayList<String> lista = new ArrayList<String>();
    lista = _receiver.showBatchesUnderPrice(priceLimit);
    for (String s : lista){
      _display.popup(s);
    }
  }
}

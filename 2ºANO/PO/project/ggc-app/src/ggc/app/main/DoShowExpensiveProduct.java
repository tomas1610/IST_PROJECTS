package ggc.app.main;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import ggc.WarehouseManager;
//FIXME import classes

/**
 * Save current state to file under current name (if unnamed, query for name).
 */
class DoShowExpensiveProduct extends Command<WarehouseManager> {

  /** @param receiver */
  DoShowExpensiveProduct(WarehouseManager receiver) {
    super(Label.SHOW_EXPENSIVEST_PRODUCT, receiver);
    }
  

  @Override
  public final void execute() throws CommandException {
    _display.popup(_receiver.showExpensiveProduct());
  }
}

package ggc.app.partners;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import ggc.WarehouseManager;

//FIXME import classes

/**
 * Register new partner.
 */
class DoChangeId extends Command<WarehouseManager> {

  DoChangeId(WarehouseManager receiver) {
    super(Label.CHANGE_ID, receiver);
    addStringField("ActualPartnerKey",Prompt.partnerKey());
    addStringField("NewPartnerKey",Prompt.partnerKey());
  }

  @Override
  public void execute() throws CommandException {
      String key = stringField("ActualPartnerKey");
      String newKey = stringField("NewPartnerKey");
      _receiver.changeId(key,newKey);
  }

}

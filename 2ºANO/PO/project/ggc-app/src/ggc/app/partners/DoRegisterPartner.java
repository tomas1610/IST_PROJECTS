package ggc.app.partners;

import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
import ggc.app.exceptions.DuplicatePartnerKeyException;
import ggc.exceptions.DuplicateUIPartnerException;
import ggc.WarehouseManager;

//FIXME import classes

/**
 * Register new partner.
 */
class DoRegisterPartner extends Command<WarehouseManager> {

  DoRegisterPartner(WarehouseManager receiver) {
    super(Label.REGISTER_PARTNER, receiver);
    addStringField("partnerKey",Prompt.partnerKey());
    addStringField("partnerName",Prompt.partnerName());
    addStringField("partnerAdress",Prompt.partnerAddress());
  }

  @Override
  public void execute() throws CommandException {
    try {
      String key = stringField("partnerKey");
      String name = stringField("partnerName");
      String adress = stringField("partnerAdress");
      _receiver.registPartner(key,name,adress);
    }
    catch (DuplicateUIPartnerException e){
      throw new DuplicatePartnerKeyException(e.getKey());
    }
  }

}

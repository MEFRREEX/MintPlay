package com.bestaford.mintplay.module;

import cn.nukkit.Player;
import cn.nukkit.utils.ConfigSection;
import cn.nukkit.utils.TextFormat;
import com.bestaford.mintplay.MintPlay;
import ru.nukkitx.forms.elements.CustomForm;
import ru.nukkitx.forms.elements.ModalForm;

import java.util.ArrayList;

public class Characters {

    private final MintPlay plugin;

    public Characters(MintPlay plugin) {
        this.plugin = plugin;
    }

    public void sendCharacterCreateForm(Player player) {
        sendCharacterCreateForm(player, null, 0);
    }

    public void sendCharacterCreateForm(Player player, String error, int faction) {
        ConfigSection charactersSection = plugin.getConfig().getSection("characters");
        ConfigSection createSection = charactersSection.getSection("create");
        ConfigSection nameSection = createSection.getSection("name");
        CustomForm form = new CustomForm();
        form.setTitle(createSection.getString("title"));
        if(error != null) {
            form.addLabel(TextFormat.RED + nameSection.getString(error));
        }
        form.addInput(nameSection.getString("name"), nameSection.getString("placeholder"));
        ArrayList<String> list = new ArrayList<>();
        list.add("Данные о расе Данные о расе Данные о расе Данные о расе Данные о расе Данные о расе Данные о расе Данные о расе Данные о расе Данные о расе Данные о расе\nстрока 2");
        list.add("строка 1\\nДанные о расе Данные о расе Данные о расе Данные о расе Данные о расе Данные о расе Данные о расе Данные о расе Данные о расе Данные о расе");
        form.addDropDown("drop", list);
        form.send(player, (targetPlayer, targetForm, data) -> {
            if(data == null) {
                sendCharacterCreateExitForm(targetPlayer);
                return;
            }
        });
    }

    public void sendCharacterCreateExitForm(Player player) {
        ConfigSection authorizationSection = plugin.getConfig().getSection("authorization");
        ConfigSection exitSection = authorizationSection.getSection("exit");
        ModalForm form = new ModalForm(exitSection.getString("title"), exitSection.getString("content"), "Да", "Нет");
        form.send(player, (targetPlayer, targetForm, data) -> {
            if(data == -1) {
                sendCharacterCreateForm(targetPlayer);
                return;
            }
            if(data == 0) {
                targetPlayer.close();
            } else {
                sendCharacterCreateForm(targetPlayer);
            }
        });
    }
}
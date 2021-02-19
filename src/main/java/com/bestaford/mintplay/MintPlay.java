package com.bestaford.mintplay;

import cn.nukkit.plugin.PluginBase;
import com.bestaford.mintplay.module.*;
import com.bestaford.mintplay.util.Database;

public class MintPlay extends PluginBase {

    public Database database;
    public Authorization authorization;
    public Locations locations;
    public Scoreboards scoreboards;
    public Characters characters;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        database = new Database(this, "players");
        authorization = new Authorization(this);
        locations = new Locations(this);
        scoreboards = new Scoreboards(this);
        characters = new Characters(this);
        getServer().getPluginManager().registerEvents(authorization, this);
        getServer().getPluginManager().registerEvents(locations, this);
        getServer().getPluginManager().registerEvents(scoreboards, this);
        /*
        TODO: персонажи
        2 глобальные расы
        4 расы
        Альянс, Эльфы, Горные кланы, Мертворожденные
        по 5 классов на каждую расу
        дд, танк, маг, убийца, саппорт
        хранить персонажей в отдельной таблице (мб)
        делать персонажа после регистрации
        список персонажей
        таблица с персонажами: хранить расу, класс, уровень, опыт, предметы, статистику
        отдельное имя каждому персонажу, setDisplayName
        разные статы разным классам и расам
        разные перки
        классы для статов и перков
        стартовые предметы для разных рас и классов
        разные скины, выбор скина

        TODO: мобы
        MRPGNPC
        точки спавна
        модельки

        TODO: NPC
        квестовые NPC
        ключевые NPC
        торговцы
        скупщик

        TODO: локации
        таверны
        4к
        1к
        land
        */
    }
}
/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.commands;

import ivorius.psychedelicraft.entities.drugs.Drug;
import ivorius.psychedelicraft.entities.drugs.DrugProperties;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class CommandDrug extends CommandBase
{

    @Override
    public String getCommandName()
    {
        return "drug";
    }

    @Override
    public String getCommandUsage(ICommandSender par1ICommandSender)
    {
        return "commands.drug.usage";
    }

    @Override
    public void processCommand(ICommandSender commandSender, String[] arguments)
    {
        if (arguments.length < 3)
            throw new WrongUsageException(getCommandUsage(commandSender));

        EntityPlayer player = getPlayer(commandSender, arguments[0]);

        String drugName = arguments[1];

        DrugProperties drugProperties = DrugProperties.getDrugProperties(player);
        if (drugProperties == null)
            return;

        Drug singleDrug = drugProperties.getDrug(drugName);
        boolean modifyAllDrugs = drugName.equalsIgnoreCase("all");
        Collection<Drug> drugs = singleDrug != null ? Arrays.asList(singleDrug) : modifyAllDrugs ? drugProperties.getAllDrugs() : null;
        if (drugs != null)
        {
            int lock = getLockMode(arguments[2], commandSender);
            if (modifyAllDrugs)
                drugName = "Drugs";

            drugProperties.hasChanges = true;

            if (lock != 0)
            {
                for (Drug drug : drugs)
                    drug.setLocked(lock == 1);
            }

            if (arguments.length >= 4)
            {
                double amount = parseDouble(commandSender, arguments[3]);
                for (Drug drug : drugs)
                    drug.setDesiredValue(amount);

                if (lock == 0)
                    commandSender.addChatMessage(new ChatComponentTranslation("commands.drug.success", player.getCommandSenderName(), drugName, String.valueOf(amount)));
                else if (lock == 1)
                    commandSender.addChatMessage(new ChatComponentTranslation("commands.drug.success.lock", player.getCommandSenderName(), drugName, String.valueOf(amount)));
                else if (lock == 2)
                    commandSender.addChatMessage(new ChatComponentTranslation("commands.drug.success.unlock", player.getCommandSenderName(), drugName, String.valueOf(amount)));
            }
            else if (lock == 0)
                throw new CommandException(getCommandUsage(commandSender), drugName); // Didn't actually do anything
            else if (lock == 1)
                commandSender.addChatMessage(new ChatComponentTranslation("commands.drug.success.lock", player.getCommandSenderName(), drugName, drugProperties.getDrugValue(drugName)));
            else if (lock == 2)
                commandSender.addChatMessage(new ChatComponentTranslation("commands.drug.success.unlock", player.getCommandSenderName(), drugName, drugProperties.getDrugValue(drugName)));
        }
        else
        {
            throw new CommandException("commands.drug.nodrug", drugName);
        }
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender par1ICommandSender, String[] arguments)
    {
        if (arguments.length == 1)
            return getListOfStringsMatchingLastWord(arguments, getPlayers());
        else if (arguments.length == 2)
        {
            try
            {
                DrugProperties drugProperties = DrugProperties.getDrugProperties(getPlayer(par1ICommandSender, arguments[0]));
                if (drugProperties != null)
                {
                    String[] drugNames = drugProperties.getAllVisibleDrugNames();
                    String[] drugNamesPlusAll = new String[drugNames.length + 1];
                    drugNamesPlusAll[0] = "All";
                    System.arraycopy(drugNames, 0, drugNamesPlusAll, 1, drugNames.length);
                    return getListOfStringsMatchingLastWord(arguments, drugNamesPlusAll);
                }
                else
                    return null;
            }
            catch (CommandException ex)
            {
                return null;
            }
        }
        else if (arguments.length == 3)
            return getListOfStringsMatchingLastWord(arguments, "set", "lock", "unlock");
        else if (arguments.length == 4)
            return getListOfStringsMatchingLastWord(arguments, "0.0", "0.5", "1.0");

        return null;
    }

    protected int getLockMode(String lockMode, ICommandSender sender)
    {
        if (lockMode.equalsIgnoreCase("set"))
            return 0;
        else if (lockMode.equalsIgnoreCase("lock"))
            return 1;
        else if (lockMode.equalsIgnoreCase("unlock"))
            return -1;
        else
            throw new WrongUsageException(getCommandUsage(sender));
    }

    protected String[] getPlayers()
    {
        return MinecraftServer.getServer().getAllUsernames();
    }
}

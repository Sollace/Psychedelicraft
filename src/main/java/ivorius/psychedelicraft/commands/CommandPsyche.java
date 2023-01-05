/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.commands;

import ivorius.psychedelicraft.entities.EntityRealityRift;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandPsyche extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return "psyche";
    }

    @Override
    public String getCommandUsage(ICommandSender par1ICommandSender)
    {
        return "/psyche <command> -- Performs a special Psychedelicraft-related command. Press tab for options.";
    }

    @Override
    public void processCommand(ICommandSender commandSender, String[] args)
    {
        if (args.length >= 1)
        {
            if ("spawnRift".equals(args[0]))
            {
                double x = (double)commandSender.getPlayerCoordinates().posX + 0.5D;
                double y = (double)commandSender.getPlayerCoordinates().posY;
                double z = (double)commandSender.getPlayerCoordinates().posZ + 0.5D;

                if (args.length >= 4)
                {
                    x = func_110666_a(commandSender, x, args[1]);
                    y = func_110666_a(commandSender, y, args[2]);
                    z = func_110666_a(commandSender, z, args[3]);
                }

                EntityRealityRift rift = new EntityRealityRift(commandSender.getEntityWorld());
                rift.setPosition(x, y, z);
                commandSender.getEntityWorld().spawnEntityInWorld(rift);
            }
        }
        else
        {
            throw new WrongUsageException(getCommandUsage(commandSender));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> addTabCompletionOptions(ICommandSender par1ICommandSender, String[] par2ArrayOfStr)
    {
        if (par2ArrayOfStr.length == 1)
        {
            String[] values = new String[]{"spawnRift"};

            List<String> list = new ArrayList<>();
            Collections.addAll(list, values);

            return list;
        }

        return null;
    }
}

package punish;

import java.util.ArrayList;
import java.util.logging.Logger;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class main extends JavaPlugin {
	public final Logger logger = Logger.getLogger("Minecraft");
	public static main plugin;
	public static Economy econ = null;
	public static Permission perms = null;
	public static Chat chat = null;
	public String prefix = ChatColor.BLUE + "[" + ChatColor.GOLD + "Punishment"
			+ ChatColor.BLUE + "]" + ChatColor.RESET;
	public static ArrayList<String> items = new ArrayList<String>();

	@Override
	public void onDisable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " Has Been Disabled!");
	}

	@Override
	public void onEnable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " Version " + pdfFile.getVersion()
				+ " Has Been Enabled!");

		getConfig().addDefault("Economy", false);
		getConfig().addDefault("slapcost", 10.00);
		getConfig().addDefault("punchcost", 15.00);
		getConfig().addDefault("spankcost", 20.00);
		getConfig().addDefault("fartcost", 30.00);
		getConfig().addDefault("itemslapcost", 30.00);
		getConfig().options().copyDefaults(true);
		saveConfig();
		if (getConfig().getBoolean("Economy")) {
			if (!setupEconomy()) {
				logger.severe(String.format(
						"[%s] - Disabled due to no Vault dependency found!",
						getDescription().getName()));
				getServer().getPluginManager().disablePlugin(this);
				return;
			}
		}
	}

	private boolean setupEconomy() {
		if (getConfig().getBoolean("Economy")) {

			if (getServer().getPluginManager().getPlugin("Vault") == null) {
				return false;
			}
			RegisteredServiceProvider<Economy> rsp = getServer()
					.getServicesManager().getRegistration(Economy.class);
			if (rsp == null) {
				return false;
			}
			econ = rsp.getProvider();
			return econ != null;
		}

		return false;
	}

	@SuppressWarnings("unused")
	public static boolean isNumeric(String str) {
		try {
			double d = Double.parseDouble(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args) {
		Player player = (Player) sender;
		double slapcost = getConfig().getInt("slapcost");
		double punchcost = getConfig().getInt("punchcost");
		double spankcost = getConfig().getInt("spankcost");
		double fartcost = getConfig().getInt("fartcost");
		double itemslapcost = getConfig().getInt("slapitemcost");

		if (commandLabel.equalsIgnoreCase("slap")) {
			if (player.isOp()
					|| player.hasPermission(new Permissions().canSlap)) {
				if (args.length == 0) {
					player.damage(4);
					player.sendMessage(prefix + ChatColor.RED
							+ "You slapped yourself!");

				} else if (args.length == 1) {
					Player targetPlayer = player.getServer().getPlayer(args[0]);
					targetPlayer.damage(4);
					player.sendMessage(prefix + "You slapped " + ChatColor.AQUA
							+ targetPlayer.getDisplayName());
					targetPlayer.sendMessage(prefix + "You were slapped by "
							+ ChatColor.RED + player.getDisplayName());
					if (getConfig().getBoolean("Economy")) {
						EconomyResponse r = econ.withdrawPlayer(
								player.getName(), slapcost);
						if (r.transactionSuccess()) {
							player.sendMessage(String.format(
									prefix + "You Successfuly Slapped "
											+ player.getDisplayName()
											+ " For %s", econ.format(r.amount)));
						} else {
							player.sendMessage(String.format(prefix
									+ "An error occured: %s", r.errorMessage));
						}
					}
				} else if (args.length == 2) {
					if (sender.hasPermission(new Permissions().canSlapItem)) {
						Material mat = Material.getMaterial(args[1].toString()
								.toUpperCase());
						if (mat == null) {
							sender.sendMessage(prefix + ChatColor.RED
									+ "That is not a real item!");
						} else {

							Player targetPlayer = player.getServer().getPlayer(
									args[0]);
							ItemStack item = new ItemStack(mat, 1);
							String type = item.getType().toString();
							targetPlayer.damage(4);
							player.sendMessage(prefix + "You slapped "
									+ ChatColor.AQUA
									+ targetPlayer.getDisplayName()
									+ ChatColor.AQUA + " with " + type);
							targetPlayer.sendMessage(prefix
									+ "You were slapped by " + ChatColor.RED
									+ player.getDisplayName() + ChatColor.WHITE
									+ " with " + type);
							ItemMeta meta = item.getItemMeta();
							String name = item.getType().toString()
									.replaceAll("_", " ");
							meta.setDisplayName(ChatColor.RED + "§lBloody "
									+ name);
							ArrayList<String> lore = new ArrayList<String>();
							lore.clear();
							lore.add(ChatColor.DARK_PURPLE
									+ "§lBlood from the face of "
									+ targetPlayer.getDisplayName());
							meta.setLore(lore);
							meta.addEnchant(Enchantment.DAMAGE_ALL, 0, true);
							item.setItemMeta(meta);
							player.getInventory().addItem(item);
							ItemStack flesh = new ItemStack(
									Material.ROTTEN_FLESH, 1);
							ItemMeta fmeta = flesh.getItemMeta();
							fmeta.setDisplayName(ChatColor.RED
									+ "§lYour Face Flesh");
							flesh.setItemMeta(fmeta);
							targetPlayer.getInventory().addItem(flesh);
							if (getConfig().getBoolean("Economy")) {
								EconomyResponse r = econ.withdrawPlayer(
										player.getName(), itemslapcost);
								if (r.transactionSuccess()) {
									player.sendMessage(String.format(prefix
											+ "You Successfuly Slapped "
											+ player.getDisplayName()
											+ " For %s", econ.format(r.amount)));
								} else {
									player.sendMessage(String.format(prefix
											+ "An error occured: %s",
											r.errorMessage));
								}
							}
						}

					} else {
						sender.sendMessage(prefix
								+ "You don't have permission to slap players with an item!");
					}
				} else if (args.length == 3) {
					if (sender.hasPermission(new Permissions().canSlapItem)) {
						Material mat = Material.getMaterial(args[1].toString()
								.toUpperCase());
						if (mat == null) {
							sender.sendMessage(prefix + ChatColor.RED
									+ "That is not a real item!");
						} else {
							Player targetPlayer = player.getServer().getPlayer(
									args[0]);
							int number = Integer.parseInt(args[1]);
							int data = Integer.parseInt(args[2]);
							ItemStack item = new ItemStack(number, 1,
									(short) data);
							String type = item.getType().toString();
							targetPlayer.damage(4);
							player.sendMessage(prefix + "You slapped "
									+ ChatColor.AQUA
									+ targetPlayer.getDisplayName()
									+ ChatColor.AQUA + " with " + type);
							targetPlayer.sendMessage(prefix
									+ "You were slapped by " + ChatColor.RED
									+ player.getDisplayName() + ChatColor.WHITE
									+ " with " + type);
							ItemMeta meta = item.getItemMeta();
							String name = item.getType().toString()
									.replaceAll("_", " ");
							meta.setDisplayName(ChatColor.RED + "§lBloody "
									+ name);
							ArrayList<String> lore = new ArrayList<String>();
							lore.clear();
							lore.add(ChatColor.DARK_PURPLE
									+ "§lBlood from the face of "
									+ targetPlayer.getDisplayName());
							meta.setLore(lore);
							meta.addEnchant(Enchantment.DAMAGE_ALL, 0, true);
							item.setItemMeta(meta);
							player.getInventory().addItem(item);
							ItemStack flesh = new ItemStack(
									Material.ROTTEN_FLESH, 1);
							ItemMeta fmeta = flesh.getItemMeta();
							fmeta.setDisplayName(ChatColor.RED
									+ "§lYour Face Flesh");
							flesh.setItemMeta(fmeta);
							targetPlayer.getInventory().addItem(flesh);
							if (getConfig().getBoolean("Economy")) {
								EconomyResponse r = econ.withdrawPlayer(
										player.getName(), itemslapcost);
								if (r.transactionSuccess()) {
									player.sendMessage(String.format(prefix
											+ "You Successfuly Slapped "
											+ player.getDisplayName()
											+ " For %s", econ.format(r.amount)));
								} else {
									player.sendMessage(String.format(prefix
											+ "An error occured: %s",
											r.errorMessage));
								}
							}
						}
					} else {
						sender.sendMessage(prefix
								+ "You don't have permission to slap players with an item!");
					}
				}

			} else {
				player.sendMessage(prefix + ChatColor.DARK_RED
						+ "You do not have permission to slap people!");
			}
		}

		if (commandLabel.equalsIgnoreCase("punch")) {
			if (player.isOp()
					|| player.hasPermission(new Permissions().canPunch)) {
				if (args.length == 0) {
					player.damage(6);
					player.sendMessage(prefix + ChatColor.RED
							+ "You punched yourself!");
				} else if (args.length == 1) {
					Player targetPlayer = player.getServer().getPlayer(args[0]);
					targetPlayer.damage(6);
					player.sendMessage(prefix + "You punched " + ChatColor.AQUA
							+ targetPlayer.getDisplayName());
					targetPlayer.sendMessage(prefix + "You were punched by "
							+ ChatColor.RED + player.getDisplayName());
					if (getConfig().getBoolean("Economy")) {
						EconomyResponse r = econ.withdrawPlayer(
								player.getName(), punchcost);
						if (r.transactionSuccess()) {
							player.sendMessage(String.format(
									prefix + "You Successfuly Punched "
											+ player.getDisplayName()
											+ " For %s", econ.format(r.amount)));
						} else {
							player.sendMessage(String.format(prefix
									+ "An error occured: %s", r.errorMessage));
						}
					}
				}
			} else {
				player.sendMessage(prefix + ChatColor.DARK_RED
						+ "You do not have permission to punch people!");
			}
		}

		if (commandLabel.equalsIgnoreCase("spank")) {
			if (player.isOp()
					|| player.hasPermission(new Permissions().canSpank)) {
				if (args.length == 0) {
					player.damage(8);
					player.sendMessage(prefix + ChatColor.RED
							+ "You spanked yourself!");
				} else if (args.length == 1) {
					Player targetPlayer = player.getServer().getPlayer(args[0]);
					targetPlayer.damage(8);
					player.sendMessage(prefix + "You spanked " + ChatColor.AQUA
							+ targetPlayer.getDisplayName());
					targetPlayer.sendMessage(prefix + "You were spanked by "
							+ ChatColor.RED + player.getDisplayName());
					if (getConfig().getBoolean("Economy")) {
						EconomyResponse r = econ.withdrawPlayer(
								player.getName(), spankcost);
						if (r.transactionSuccess()) {
							player.sendMessage(String.format(
									prefix + "You Successfuly Spanked "
											+ player.getDisplayName()
											+ " For %s", econ.format(r.amount)));
						} else {
							player.sendMessage(String.format(prefix
									+ "An error occured: %s", r.errorMessage));
						}
					}
				}
			} else {
				player.sendMessage(prefix + ChatColor.DARK_RED
						+ "You do not have permission to spank people!");
			}
		}

		if (commandLabel.equalsIgnoreCase("fart")) {

			if (player.isOp()
					|| player.hasPermission(new Permissions().canFart)) {
				if (args.length == 0) {
					player.addPotionEffect(new PotionEffect(
							PotionEffectType.CONFUSION, 500, 1));
					player.sendMessage(prefix + ChatColor.DARK_GREEN
							+ "You farted on yourself!");
				} else if (args.length == 1) {
					Player targetPlayer = player.getServer().getPlayer(args[0]);
					targetPlayer.addPotionEffect(new PotionEffect(
							PotionEffectType.CONFUSION, 500, 1));
					player.sendMessage(prefix + "You farted on "
							+ ChatColor.DARK_GREEN
							+ targetPlayer.getDisplayName());
					targetPlayer.sendMessage(prefix + "You were farted on by "
							+ ChatColor.DARK_GREEN + player.getDisplayName());
					if (getConfig().getBoolean("Economy")) {
						EconomyResponse r = econ.withdrawPlayer(
								player.getName(), fartcost);
						if (r.transactionSuccess()) {
							player.sendMessage(String.format(
									prefix + "You Successfuly Farted on "
											+ player.getDisplayName()
											+ " For %s", econ.format(r.amount)));
						} else {
							player.sendMessage(String.format(prefix
									+ "An error occured: %s", r.errorMessage));
						}
					}
				}
			} else {
				player.sendMessage(prefix + ChatColor.DARK_RED
						+ "You do not have permission to fart on people!");
			}
		}

		if (commandLabel.equalsIgnoreCase("artpun")) {
			if (args.length == 0) {
				player.sendMessage(ChatColor.AQUA + "--------" + prefix
						+ ChatColor.AQUA + "--------");
				player.sendMessage(ChatColor.GOLD
						+ "+ KEY: <> - REQUIRED [] - OPTIONAL");
				player.sendMessage(ChatColor.GOLD
						+ "+ /artpun - List the Commands for Artificial Punishment");
				player.sendMessage(ChatColor.GOLD
						+ "+ /slap [playername] [ItemName [DamageValue]] - Slap a player or yourself! Use '_' for spaces!");
				player.sendMessage(ChatColor.GOLD
						+ "+ /punch [playername] - Punch a player or yourself");
				player.sendMessage(ChatColor.GOLD
						+ "+ /spank [playername] - Spank a player or yourself!");
				player.sendMessage(ChatColor.GOLD
						+ "+ /fart [playername] - Fart on a player or yourself!");
				player.sendMessage(ChatColor.AQUA + "--------" + prefix
						+ ChatColor.AQUA + "--------");
			} else if (args.length == 1) {
				if (args[0].equalsIgnoreCase("reload")) {
					if (player.isOp()
							|| player
									.hasPermission(new Permissions().canReloadPlugin))
						reloadConfig();
					player.sendMessage(prefix + ChatColor.GREEN
							+ " has been reloaded!");
				} else {
					player.sendMessage(prefix
							+ ChatColor.RED
							+ "You Do Not Have Permission To Reload The Plugin!");
				}
			} else {
				player.sendMessage(prefix + ChatColor.RED
						+ "Please use /artpun to list the commands!");
			}
		}

		return false;
	}

}

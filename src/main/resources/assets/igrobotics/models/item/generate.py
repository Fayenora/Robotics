metals = ["tin", "gold", "copper", "tin", "aluminium", "nickel", "silver", "lead", "bronze", "constantan", "steel", "electrum", "platinum", "irdium", "signalum", "lumium", "ardite", "cobalt", "osmium", "manyullin", "dark_steel", "tough_alloy", "end_steel", "iridium", "psimetal"]



for metal in metals:
    filename = f"plate_{metal}.json"
    with open(filename, 'w') as writer:
        text = "{\"parent\": \"item/generated\", \"textures\": {\"layer0\": \"igrobotics:item/strong_platings/strong_plate_" + metal + "\"}}"
        writer.write(text)
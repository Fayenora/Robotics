import os

materials = [
    "iron", 
    "gold", 
    "copper", 
    "tin", 
    "aluminium", 
    "nickel", 
    "silver", 
    "lead", 
    "bronze", 
    "constantan", 
    "steel", 
    "electrum", 
    "platinum", 
    "iridium", 
    "signalum", 
    "lumium", 
    "dark_steel", 
    "end_steel", 
    "tough_alloy", 
    "cobalt", 
    "ardite", 
    "manyullin", 
    "osmium", 
    "psimetal"
]

parts = [
    "head", 
    "body", 
    "left_arm", 
    "right_arm", 
    "left_leg", 
    "right_leg"
]

for part in parts:
    for material in materials:
        texture_name = "igrobotics:item/robot/" + material + "/robot_" + part
        file_contents = "{\n\t\"parent\": \"item/generated\",\n\t\"textures\": {\n\t\t\"layer0\": \"" + texture_name + "\"\n\t}\n}\n"
        file_name = f"{material}_{part}.json"
        with open(file_name, 'w') as f:
            f.write(file_contents)

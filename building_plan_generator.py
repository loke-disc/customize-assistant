import matplotlib.pyplot as plt
import matplotlib.patches as patches
from matplotlib.patches import Rectangle, FancyBboxPatch
import numpy as np

def create_building_plan():
    # Land dimensions
    land_width = 80  # North side (ft)
    land_depth = 55  # West side (ft)
    
    # Setback requirements (typical for residential)
    front_setback = 10  # from north road
    side_setback = 8   # from west road
    rear_setback = 6   # from south boundary
    east_setback = 5   # from east boundary
    
    # Calculate buildable area
    buildable_width = land_width - side_setback - east_setback  # 80 - 8 - 5 = 67ft
    buildable_depth = land_depth - front_setback - rear_setback  # 55 - 10 - 6 = 39ft
    buildable_area = buildable_width * buildable_depth  # 67 × 39 = 2613 sqft
    
    # Building dimensions (leaving some margin for landscaping)
    building_width = 62  # ft
    building_depth = 36  # ft
    building_area = building_width * building_depth  # 2232 sqft
    
    # Create figure and axis
    fig, ax = plt.subplots(1, 1, figsize=(16, 12))
    
    # Draw land boundary
    land_rect = Rectangle((0, 0), land_width, land_depth, 
                         linewidth=3, edgecolor='black', facecolor='lightgreen', alpha=0.3)
    ax.add_patch(land_rect)
    
    # Draw roads
    # North road (30ft wide)
    north_road = Rectangle((0, land_depth), land_width, 30, 
                          linewidth=2, edgecolor='gray', facecolor='darkgray', alpha=0.7)
    ax.add_patch(north_road)
    
    # West road (23ft wide)
    west_road = Rectangle((-23, 0), 23, land_depth, 
                         linewidth=2, edgecolor='gray', facecolor='darkgray', alpha=0.7)
    ax.add_patch(west_road)
    
    # Building position
    building_x = side_setback + 1  # 9ft from west boundary
    building_y = front_setback + 1  # 11ft from north boundary
    
    # Draw building outline
    building_rect = Rectangle((building_x, building_y), building_width, building_depth,
                            linewidth=2, edgecolor='darkblue', facecolor='lightblue', alpha=0.8)
    ax.add_patch(building_rect)
    
    # Room dimensions and positions
    rooms = [
        # Ground Floor
        {'name': 'Living Room', 'x': building_x, 'y': building_y + 20, 'width': 18, 'height': 16, 'color': 'wheat'},
        {'name': 'Kitchen', 'x': building_x + 18, 'y': building_y + 20, 'width': 14, 'height': 16, 'color': 'lightcoral'},
        {'name': 'Dining', 'x': building_x + 32, 'y': building_y + 20, 'width': 12, 'height': 16, 'color': 'lightyellow'},
        {'name': 'Master Bedroom', 'x': building_x + 44, 'y': building_y + 20, 'width': 18, 'height': 16, 'color': 'lavender'},
        
        {'name': 'Bedroom 2', 'x': building_x, 'y': building_y, 'width': 15, 'height': 20, 'color': 'lightcyan'},
        {'name': 'Bedroom 3', 'x': building_x + 15, 'y': building_y, 'width': 15, 'height': 20, 'color': 'lightcyan'},
        {'name': 'Bathroom 1', 'x': building_x + 30, 'y': building_y, 'width': 8, 'height': 10, 'color': 'lightsteelblue'},
        {'name': 'Bathroom 2', 'x': building_x + 38, 'y': building_y, 'width': 8, 'height': 10, 'color': 'lightsteelblue'},
        {'name': 'Study/Office', 'x': building_x + 46, 'y': building_y, 'width': 16, 'height': 12, 'color': 'lightgoldenrodyellow'},
        {'name': 'Storage', 'x': building_x + 30, 'y': building_y + 10, 'width': 8, 'height': 10, 'color': 'lightgray'},
        {'name': 'Utility', 'x': building_x + 38, 'y': building_y + 10, 'width': 8, 'height': 10, 'color': 'lightgray'},
    ]
    
    # Draw rooms
    for room in rooms:
        room_rect = Rectangle((room['x'], room['y']), room['width'], room['height'],
                            linewidth=1, edgecolor='black', facecolor=room['color'], alpha=0.7)
        ax.add_patch(room_rect)
        
        # Add room labels
        ax.text(room['x'] + room['width']/2, room['y'] + room['height']/2, room['name'],
                ha='center', va='center', fontsize=9, fontweight='bold', 
                bbox=dict(boxstyle="round,pad=0.3", facecolor='white', alpha=0.8))
    
    # Draw parking area
    parking_x = building_x + building_width + 2
    parking_y = building_y + 10
    parking_rect = Rectangle((parking_x, parking_y), 6, 20,
                           linewidth=2, edgecolor='brown', facecolor='tan', alpha=0.6)
    ax.add_patch(parking_rect)
    ax.text(parking_x + 3, parking_y + 10, 'Parking', ha='center', va='center', 
            fontsize=10, fontweight='bold', rotation=90)
    
    # Draw garden/landscape areas
    # Front garden
    front_garden = Rectangle((side_setback, building_y + building_depth + 1), 
                           building_width, front_setback - 2,
                           linewidth=1, edgecolor='green', facecolor='lightgreen', alpha=0.5)
    ax.add_patch(front_garden)
    ax.text(side_setback + building_width/2, building_y + building_depth + 3, 'Front Garden',
            ha='center', va='center', fontsize=10, style='italic')
    
    # Side garden
    side_garden = Rectangle((building_x + building_width + 8, building_y), 
                          land_width - (building_x + building_width + 8) - east_setback, 
                          building_depth,
                          linewidth=1, edgecolor='green', facecolor='lightgreen', alpha=0.5)
    ax.add_patch(side_garden)
    ax.text(building_x + building_width + 12, building_y + building_depth/2, 'Side\nGarden',
            ha='center', va='center', fontsize=10, style='italic')
    
    # Add dimensions and labels
    # Land dimensions
    ax.annotate('', xy=(0, -3), xytext=(land_width, -3), 
                arrowprops=dict(arrowstyle='<->', color='red', lw=2))
    ax.text(land_width/2, -5, f'{land_width} ft', ha='center', va='top', 
            fontsize=12, fontweight='bold', color='red')
    
    ax.annotate('', xy=(-5, 0), xytext=(-5, land_depth), 
                arrowprops=dict(arrowstyle='<->', color='red', lw=2))
    ax.text(-7, land_depth/2, f'{land_depth} ft', ha='right', va='center', 
            fontsize=12, fontweight='bold', color='red', rotation=90)
    
    # Building dimensions
    ax.annotate('', xy=(building_x, building_y - 2), xytext=(building_x + building_width, building_y - 2), 
                arrowprops=dict(arrowstyle='<->', color='blue', lw=1.5))
    ax.text(building_x + building_width/2, building_y - 4, f'{building_width} ft', 
            ha='center', va='top', fontsize=10, fontweight='bold', color='blue')
    
    # Road labels
    ax.text(land_width/2, land_depth + 15, '30 ft Road (North)', 
            ha='center', va='center', fontsize=12, fontweight='bold', color='darkgray')
    ax.text(-11.5, land_depth/2, '23 ft Road\n(West)', ha='center', va='center', 
            fontsize=12, fontweight='bold', color='darkgray')
    
    # Setback indicators
    ax.plot([0, land_width], [front_setback, front_setback], 'r--', alpha=0.5, linewidth=1)
    ax.plot([side_setback, side_setback], [0, land_depth], 'r--', alpha=0.5, linewidth=1)
    ax.plot([land_width - east_setback, land_width - east_setback], [0, land_depth], 'r--', alpha=0.5, linewidth=1)
    ax.plot([0, land_width], [rear_setback, rear_setback], 'r--', alpha=0.5, linewidth=1)
    
    # Add compass
    ax.text(land_width - 8, land_depth - 5, 'N', ha='center', va='center', 
            fontsize=16, fontweight='bold', 
            bbox=dict(boxstyle="circle,pad=0.3", facecolor='white', edgecolor='black'))
    ax.arrow(land_width - 8, land_depth - 8, 0, 3, head_width=1, head_length=1, fc='black', ec='black')
    
    # Title and information
    plt.title('Building Plan - 4400 sqft Corner Plot\n80ft × 55ft Land with Road Access', 
              fontsize=16, fontweight='bold', pad=20)
    
    # Information box
    info_text = f"""Land Area: {land_width} × {land_depth} = {land_width * land_depth} sqft
Building Area: {building_width} × {building_depth} = {building_area} sqft
Buildable Area: {buildable_width} × {buildable_depth} = {buildable_area} sqft
Coverage Ratio: {(building_area / (land_width * land_depth) * 100):.1f}%

Setbacks:
• Front (North): {front_setback} ft
• Side (West): {side_setback} ft  
• Rear (South): {rear_setback} ft
• Side (East): {east_setback} ft"""
    
    ax.text(0.02, 0.98, info_text, transform=ax.transAxes, fontsize=10,
            verticalalignment='top', bbox=dict(boxstyle="round,pad=0.5", 
            facecolor='lightyellow', alpha=0.9))
    
    # Set axis properties
    ax.set_xlim(-25, land_width + 5)
    ax.set_ylim(-8, land_depth + 35)
    ax.set_aspect('equal')
    ax.grid(True, alpha=0.3)
    ax.set_xlabel('Width (feet)', fontsize=12)
    ax.set_ylabel('Depth (feet)', fontsize=12)
    
    # Remove top and right spines
    ax.spines['top'].set_visible(False)
    ax.spines['right'].set_visible(False)
    
    plt.tight_layout()
    return fig

# Generate and save the building plan
if __name__ == "__main__":
    fig = create_building_plan()
    plt.savefig('/workspace/building_plan_4400sqft.png', dpi=300, bbox_inches='tight', 
                facecolor='white', edgecolor='none')
    plt.savefig('/workspace/building_plan_4400sqft.pdf', bbox_inches='tight', 
                facecolor='white', edgecolor='none')
    plt.show()
    print("Building plan diagram generated successfully!")
    print("Files saved: building_plan_4400sqft.png and building_plan_4400sqft.pdf")
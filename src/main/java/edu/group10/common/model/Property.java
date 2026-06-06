package edu.group10.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.group10.common.enums.CardType;
import edu.group10.common.enums.PropertyColor;

/**
 * 物业卡模型（common 层基类）
 *
 * 物业卡是 Monopoly Deal 游戏的核心资产类型。
 * 玩家通过收集同一颜色的全套物业来获得胜利。
 *
 * 物业卡分为三种：
 * 1. 单色物业卡 —— 只有一种颜色，如"红色物业"、"蓝色物业"
 * 2. 双色物业卡 —— 可在两种颜色间切换，如"粉/橙双色物业"
 * 3. 万能物业卡 —— 可作为任意颜色使用
 *
 * 注意：这个类是 common 层的公共模型，存放于 common/model 而非 core/card 中，
 * 因为前端也需要知道物业卡的基本信息（用于渲染展示）。
 * core/card/PropertyCard 继承自本类，添加了核心层特有的计分逻辑。
 */
public class Property extends Card {

    /** 主颜色（所有物业卡必须有一个主颜色） */
    @JsonProperty
    private PropertyColor primaryColor;

    /**
     * 副颜色（可选）
     * - 双色物业卡：副颜色为另一种可选的 PropertyColor
     * - 万能物业卡：副颜色为 null，因为万能卡可以作为任意颜色
     * - 单色物业卡：副颜色为 null
     */
    @JsonProperty
    private PropertyColor secondaryColor;

    /** 当前生效的颜色 —— 双色卡/万能卡被玩家指定颜色后会切换此值 */
    @JsonProperty
    private PropertyColor currentColor;

    /** 租金值（出此物业对应的租金卡时，每张此颜色的物业应收的金额） */
    @JsonProperty
    private int rent;

    /** 该颜色全套物业需要的总张数（如棕色需要2张，红色需要3张） */
    @JsonProperty
    private int setSize;

    /** 是否有房子（租金 +3M） */
    @JsonProperty
    private boolean hasHouse;

    /** 是否有旅馆（租金 +4M） */
    @JsonProperty
    private boolean hasHotel;

    // ======================== 构造器 ========================

    /**
     * 无参构造器（Jackson 反序列化需要）
     */
    public Property() {
        super();
        this.hasHouse = false;
        this.hasHotel = false;
    }

    /**
     * 完整构造器
     *
     * @param cardId    卡牌唯一标识，如 "brown_1"
     * @param cardName  卡牌显示名称，如 "Brown Property"
     * @param primary   主颜色
     * @param secondary 副颜色（双色卡需要；单色/万能卡传 null）
     * @param rent      每张此颜色物业的租金值
     * @param setSize   该颜色全套需要的张数
     */
    public Property(String cardId, String cardName, PropertyColor primary,
                    PropertyColor secondary, int rent, int setSize) {
        super(cardId, cardName, CardType.PROPERTY, rent);
        this.primaryColor = primary;
        this.secondaryColor = secondary;
        this.currentColor = primary;  // 初始时，当前颜色与主颜色一致
        this.rent = rent;
        this.setSize = setSize;
    }

    // ======================== 核心方法 ========================

    /**
     * 判断此物业卡是否为双色物业卡
     *
     * 双色物业卡有两个可选颜色，玩家打出时可以选择其中一个作为当前生效的颜色。
     * 例如：粉/橙双色物业卡，既可以当粉色的用，也可以当橙色的用。
     *
     * @return true 表示是双色卡（secondaryColor != null 且不是 WILD）
     */
    @JsonIgnore
    public boolean isDualColor() {
        // 万能物业卡（WILD）的 secondaryColor 虽然是 null，
        // 但它也是一种"可选颜色"的卡，与双色卡类似但更灵活
        return secondaryColor != null && secondaryColor != PropertyColor.WILD;
    }

    /**
     * 判断此物业卡是否为万能物业卡
     *
     * 万能物业卡可以当作任意颜色使用，是最灵活的物业卡。
     *
     * @return true 表示是万能物业卡
     */
    @JsonIgnore
    public boolean isWild() {
        return primaryColor == PropertyColor.WILD;
    }

    /**
     * 切换当前生效的颜色
     *
     * 当玩家打出双色/万能物业卡时，可以选择一个具体的颜色。
     * 此方法在执行选择时被 PropertyHandler 调用。
     *
     * 约束：
     * - 双色卡：只能切换到 primaryColor 或 secondaryColor 中的一个
     * - 万能卡：可以切换到任意颜色
     *
     * @param color 玩家选择的颜色
     */
    public void switchColor(PropertyColor color) {
        if (color != null) {
            this.currentColor = color;
        }
    }

    /**
     * 获取当前生效的颜色
     *
     * 对单色物业卡来说，currentColor 始终等于 primaryColor。
     * 对双色/万能物业卡来说，currentColor 可能已被 switchColor() 修改。
     *
     * 前端渲染物业卡、计算成套数量时都应使用此方法而非 getPrimaryColor()。
     *
     * @return 当前生效的颜色
     */
    public PropertyColor getCurrentColor() {
        return currentColor != null ? currentColor : primaryColor;
    }

    /**
     * 计算带房子/旅馆的租金
     * 用于租金卡计算应收金额
     *
     * @return 最终租金（基础租金 + 房子加成 + 旅馆加成）
     */
    @JsonIgnore
    public int getRentWithModifiers() {
        int finalRent = rent;
        if (hasHouse) finalRent += 3;
        if (hasHotel) finalRent += 4;
        return finalRent;
    }

    // ======================== Getters & Setters ========================

    public PropertyColor getPrimaryColor() {
        return primaryColor;
    }

    public void setPrimaryColor(PropertyColor primaryColor) {
        this.primaryColor = primaryColor;
    }

    public PropertyColor getSecondaryColor() {
        return secondaryColor;
    }

    public void setSecondaryColor(PropertyColor secondaryColor) {
        this.secondaryColor = secondaryColor;
    }

    public void setCurrentColor(PropertyColor currentColor) {
        this.currentColor = currentColor;
    }

    public int getRent() {
        return rent;
    }

    public void setRent(int rent) {
        this.rent = rent;
    }

    public int getSetSize() {
        return setSize;
    }

    public void setSetSize(int setSize) {
        this.setSize = setSize;
    }

    public boolean isHasHouse() {
        return hasHouse;
    }

    public void setHasHouse(boolean hasHouse) {
        this.hasHouse = hasHouse;
    }

    public boolean isHasHotel() {
        return hasHotel;
    }

    public void setHasHotel(boolean hasHotel) {
        this.hasHotel = hasHotel;
    }

    @Override
    public String toString() {
        return "Property{" +
                "cardId='" + getCardId() + '\'' +
                ", name='" + getCardName() + '\'' +
                ", primaryColor=" + primaryColor +
                ", secondaryColor=" + secondaryColor +
                ", currentColor=" + currentColor +
                ", rent=" + rent +
                ", setSize=" + setSize +
                ", hasHouse=" + hasHouse +
                ", hasHotel=" + hasHotel +
                '}';
    }
}
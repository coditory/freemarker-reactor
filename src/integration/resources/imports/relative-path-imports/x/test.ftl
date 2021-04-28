<@import name="../a" ns="a" />
<@import name="./b" ns="b" />
<@import name="./y/c" ns="c" />
<@import name="../x/y/../b" ns="b2" />
Template with relative imports
<@a.ma />
<@b.mb />
<@c.mc />
<@b2.mb />
import numpy as np

a = [[[1,5,6],[8,1,8],[2,6,7],[2,6,8]],[[2,4,3],[2,5,3],[2,5,3],[2,5,3]]]
t = [[2,5,3],[3,1,4]]
a=np.array(a,dtype=np.uint8)
b=np.array(t,dtype=np.uint8)
z=0

for i in a :
    for j in i:
        if np.array_equal(j,b[0]):
            print("A")
        else:
            print("B")

print(str(a[1][3]))

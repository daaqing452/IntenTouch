import matplotlib.pyplot as plt
import sys
import numpy as np
import struct

H = 32
W = 18

f = open(sys.argv[1], 'rb')
a = f.read()
f.close()

accs = []
gyrs = []

first = True
for b in range(0, len(a), 4+12+12+H*W*2):
	t = (int(a[b+0]) << 24) + (int(a[b+1]) << 16) + (int(a[b+2]) << 8) + int(a[b+3])
	# print(t)
	acc = []
	gyr = []
	for i in range(3):
		acc.append( struct.unpack('>f', a[b+ 4+i*4 : b+ 4+i*4+4])[0] )
	for i in range(3):
		gyr.append( struct.unpack('>f', a[b+16+i*4 : b+16+i*4+4])[0] )
	accs.append(acc)
	gyrs.append(gyr)
	c = np.zeros((H, W))
	for i in range(H*W):
		x = i // W
		y = i % W
		d = (int(a[b+28+i*2]) << 8) + int(a[b+28+i*2+1])
		if d > 32767: d -= 65536
		c[x, y] = d
	
	if b % 64 == 0:
		fig, ax = plt.subplots()
		im = ax.imshow(c, vmin=-100, vmax=4000)
		ax.figure.colorbar(im, ax=ax)
		if first:
		 	ax.figure.colorbar(im, ax=ax)
		 	first = False
		plt.ion()
		plt.pause(0.01)

acc = np.array(acc)
gyr = np.array(gyr)
plt.figure()
plt.subplot(211)
plt.plot(accs)
plt.subplot(212)
plt.plot(gyrs)
plt.show()
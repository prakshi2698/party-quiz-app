# use node 18 as base image
FROM node:18

# set working directory inside container
WORKDIR /app

# copy package.json first
# docker caches this layer so npm install only reruns if dependencies change
COPY package*.json ./

# install dependencies
RUN npm install

# copy rest of the code
COPY . .

# expose port 3001 so EC2 can receive connections
EXPOSE 3001

# command to start the server
CMD ["node", "index.js"]
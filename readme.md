[![Build Status](https://travis-ci.com/zheli/shijianji.svg?branch=master)](https://travis-ci.com/zheli/shijianji)

![](https://vignette.wikia.nocookie.net/doraemon/images/f/fe/Images.jpeg/revision/latest?cb=20130708030507&path-prefix=en)

# Roadmap
- [x] CoinbasePro(former GDAX) fetcher
- [x] Base transaction model
- [x] CSV transaction exporter
- [ ] Ethereum walllet eth transaction support
- [ ] Ethereum ICO support
- [ ] User support
- [ ] Binance fetcher
- [ ] Bittrex fetcher
- [ ] Coin balance by accounts (exchange, wallet, etc)
- [ ] Database support
  - [ ] PostgreSQL support

# Run project

## Config API credentials for the integrations
* CoinbasePro
* Etherscan

## Launch server
```$xslt
sbt
sbt:shijianji> run
```

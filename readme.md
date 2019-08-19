[![Build Status](https://travis-ci.com/zheli/shijianji.svg?branch=master)](https://travis-ci.com/zheli/shijianji)

![](https://vignette.wikia.nocookie.net/doraemon/images/f/fe/Images.jpeg/revision/latest?cb=20130708030507&path-prefix=en)

# Roadmap
- [x] CoinbasePro(former GDAX) fetcher
- [x] Base transaction model
- [x] CSV transaction exporter
- [ ] CSV portfolio exporter
  - [ ] Token balances and current value in fiat
- [ ] Ethereum walllet eth transaction support
- [ ] Ethereum ICO support
- [ ] User support
- [ ] Binance fetcher
- [ ] Bittrex fetcher
- [ ] Coin balance by accounts (exchange, wallet, etc)
- [ ] Portfolio history
- [ ] Database support
  - [ ] PostgreSQL support

# Run project

## Config API credentials for the integrations
* CoinbasePro
* Etherscan

For local development, you can add a `dev.conf` file in src/man/resources folder:
```
shijianji {
  integrations {
    coinmarketcap {
      key = "key here"
      sandboxKey = "sandbox key here"
      useSandbox = true
    }

    coinbasepro {
      pass = "pass here"
      apiKey = "key here"
      apiSecret = "secret here"
    }

    etherscan {
      apikey = "key here"
    }
  }
}
```

## Launch server
```$xslt
sbt
sbt:shijianji> run
```
